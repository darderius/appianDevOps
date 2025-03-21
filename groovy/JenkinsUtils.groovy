#!/usr/bin/env groovy

void runTestsVNC(propertyFile) {
  sh "cp devops/f4a/" + propertyFile + " f4a/FitNesseForAppian/fitnesse-automation.properties"
  dir("f4a/FitNesseForAppian") {
    wrap([$class:'Xvnc', useXauthority: true]) {
      sh script: "bash ./runFitNesseTest.sh"
    }
  }
} 

void runTestsDocker(propertyFile) {
  sh "cp devops/f4a/" + propertyFile + " f4a/FitNesseForAppian/fitnesse-automation.properties"
  sh "docker-compose -f docker/docker-compose.yml up &"
  timeout(2) { //timeout is in minutes
    waitUntil {
      def numExpectedContainers = "2"
      def runningContainers = sh script: "docker ps --format {{.Names}} | grep \"fitnesse-\\(chrome\\|firefox\\)\" | wc -l", returnStdout: true
      runningContainers = runningContainers.trim()
      return (runningContainers == numExpectedContainers)
    }
  }
  sleep(10)
  dir("f4a/FitNesseForAppian") {
    sh script: "bash ./runFitNesseTest.sh"
  }
}

void retrieveLogs(propertyFile) {
  def test = sh script: "cat \"devops/f4a/${propertyFile}\" | grep \"testPath=\" | cut -d'=' -f2", returnStdout: true
  test = test.trim().minus(~"\\?.*")
  def zipName = "${test}_Results.zip"
  dir("f4a/FitNesseForAppian/FitNesseRoot/files/testResults") {
    sh "zip -r ${zipName} ${test}/**"
  }
  return "f4a/FitNesseForAppian/FitNesseRoot/files/testResults/${zipName}"
}

void buildPackage(versionPropertyFile) {
  sh "cp devops/adm/" + versionPropertyFile + " adm/appian-version-client/version-manager.properties"
  dir("adm/appian-version-client") {
    setProperty("version-manager.properties", "vcUsername", "${REPOUSERNAME}")
    setProperty("version-manager.properties", "vcPassword", "${REPOPASSWORD}")
    setProperty("version-manager.properties", "appianObjectsRepoPath", "appian/applications/${APPLICATIONNAME}")
    sh "./version-application.sh -package_path ./app-package.zip -local_repo_path ./local-repo"
    sh "unzip ./app-package.zip"
    sh "mv application* ../deploy-package.zip"
    sh "rm -rf newBundle"
    sh "mkdir newBundle"
    sh "unzip ../deploy-package.zip -d newBundle"
    sh "rm -rf newBundle/appian"
    zip zipFile: '../finalPackage.zip', archive: false, dir: 'newBundle'
  }
}
void inspectPackage(customProperties) {
  inspectionUrl = SITEBASEURL +"/deployment-management/v1/inspections"
  String response = null
  if (fileExists("appian/properties/${APPLICATIONNAME}/" + customProperties)) {
  	println "Properties Exist"
	response=sh( script:"curl --location  --request POST \"$inspectionUrl\" --header \"Appian-API-Key: $APIKEY\" --form \"zipFile=@\"adm/$PACKAGEFILENAME\"\" --form \"ICF=@\"appian/properties/${APPLICATIONNAME}/${customProperties}\"\" --form \"json={\"packageFileName\":\"$PACKAGEFILENAME\",\"customizationFileName\":\"$customProperties\"}\"", returnStdout: true).trim()
  } else{
	println "Properties does not Exist"
  	response=sh( script:"curl --location  --request POST \"$inspectionUrl\" --header \"Appian-API-Key: $APIKEY\" --form \"zipFile=@\"adm/$PACKAGEFILENAME\"\" --form \"json={\"packageFileName\":\"$PACKAGEFILENAME\"}\"", returnStdout: true).trim()
  }
  println "respuesta " + response
  //.readLines().drop(1).join(" ")
  initiateInspectionJson = new groovy.json.JsonSlurperClassic().parseText(response)
  
  println "Inspection Started"
  sleep 5
  String newUrl = SITEBASEURL + "/deployment-management/v1/inspections" + "/" + initiateInspectionJson.uuid +"/"
  String inspectionResponse = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
  inspectionResponse = inspectionResponse
  //.readLines().drop(1).join(" ")
  inspectionResponseJson = new groovy.json.JsonSlurperClassic().parseText(inspectionResponse)
  inspectionStatus = inspectionResponseJson.status
  while(inspectionStatus.equals("IN_PROGRESS")) {
    sleep 30
    inspectionResponse = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
    inspectionResponse = inspectionResponse.readLines().drop(1).join(" ")
    inspectionResponseJson = new groovy.json.JsonSlurperClassic().parseText(inspectionResponse)
    inspectionStatus = inspectionResponseJson.status
  }
  println inspectionResponseJson
  warnings = inspectionResponseJson.summary.problems.totalWarnings
  errors = inspectionResponseJson.summary.problems.totalErrors
  if(warnings.equals(0) && errors.equals(0)) {
    println "Inspection Success"
  } else{
    error "Inspection Failed, Pipeline Stopped"
  }
        
}

void createDeployment(customProperties) {
  deploymentUrl = SITEBASEURL + "/deployment-management/v1/deployments"
  if (fileExists("appian/properties/${APPLICATIONNAME}/" + customProperties)) {
    print "fileExists"
	//response=sh( script:"curl --silent --location  --request POST \'$deploymentUrl\' --header \'Appian-API-Key: $APIKEY\' --form \'zipFile=@\"adm/$PACKAGEFILENAME\"\' --form \'ICF=@\"appian/properties/${APPLICATIONNAME}/${customProperties}\"\' --form \'json={\"name\":\"newDeploymentUnix\",\"packageFileName\":\"$PACKAGEFILENAME\",\"customizationFileName\":\"$customProperties\"}\'", returnStdout: true).trim()
	response=sh( script:"curl --location  --request POST \'$deploymentUrl\' --header \'Appian-API-Key: $APIKEY\' --form \'zipFile=@\"adm/$PACKAGEFILENAME\"\' --form \'ICF=@\"appian/properties/${APPLICATIONNAME}/${customProperties}\"\' --form \'json={\"name\":\"newDeploymentUnix\",\"packageFileName\":\"$PACKAGEFILENAME\",\"customizationFileName\":\"$customProperties\"}\'", returnStdout: true).trim()
  } else{
   //response=sh( script:"curl --silent  --location  --request POST \"$deploymentUrl\" --header \"Appian-API-Key: $APIKEY\" --form \"zipFile=@\"adm/$PACKAGEFILENAME\"\" --form \"json={\"packagFileName\":\"$PACKAGEFILENAME\",\"name\":\"$DEPLOYMENTNAME\"}\"", returnStdout: true).trim()
   response=sh( script:"curl --location  --request POST \"$deploymentUrl\" --header \"Appian-API-Key: $APIKEY\" --form \"zipFile=@\"adm/$PACKAGEFILENAME\"\" --form \"json={\"packageFileName\":\"$PACKAGEFILENAME\",\"name\":\"$DEPLOYMENTNAME\"}\"", returnStdout: true).trim()

  }
  println response
  //.readLines().drop(1).join(" ")
  deploymentResponseJson = new groovy.json.JsonSlurperClassic().parseText(response)
  println "Deployment Requested"
}

void requestPatchAnalysis() {
	println "requestPatchAnalysis"
	
	//URL de Aquaman, donde solicitaremos el analisis del parche que subimos
   aquamanUrl = SITEBASEURL + "/webapi/analysePatch?applicationUuid=$APPLICATIONUUID" 
   response=sh( script:"curl --location  --request POST \"$aquamanUrl\" --header \"Appian-Document-Name:$PACKAGEFILENAME\" --header \"Appian-API-Key: $APIKEY\" --data-binary @\"adm/$PACKAGEFILENAME\"", returnStdout: true).trim()
   println "Respuesta recibida"
    println response
   //.readLines().drop(1).join(" ")
   analysisStatusJson = new groovy.json.JsonSlurperClassic().parseText(response)
   patchId = analysisStatusJson.patchId
   
   checkAnalyzePatchStatus(patchId)
  
  
  println "Source Code analysis finished"
}

void checkAnalyzePatchStatus(patchId) {
  sleep 15
  String newUrl = SITEBASEURL + "/webapi/getPatchAnalysisSummary?id=" + "/" + patchId +"/"
  String analysisStatus = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
  analysisStatus = analysisStatus
  //.readLines().drop(1).join(" ")
  analysisStatusJson = new groovy.json.JsonSlurperClassic().parseText(analysisStatus)
  statusVar = analysisStatusJson.status
  println statusVar

  while (!statusVar.equals("Completed")) {
    sleep 30
	println "statusVar:" + statusVar
	println statusVar.equals("Completed")
    analysisStatus = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
	analysisStatusJson = new groovy.json.JsonSlurperClassic().parseText(analysisStatus)
    println analysisStatusJson
	statusVar = analysisStatusJson.status
	
  }
  
  summaryVar = analysisStatusJson.summary
  
  if(summaryVar.equals("Fail")){
	
	//Devolvemos codigo de error dado que ha fallado el analisis en Aquaman para abortar el resto de la ejecucion
	error("Code analysis failed")
  }
  //Mostramos los resultados del analisis
	//getAnalysisDetails(patchId)
	//En vez de esto, mostramos un link a un reporte con la informacion para el usuario
	println '************************************************************************************************************'
	println 'Haga click a continuacion para ver los resultados del analisis'
	println 'https://everisspaindemo.appianportals.com/dc6d9daf-ab46-4a19-8db3-4769d03bc59a-portalAQ?patchId=' + patchId
	println '************************************************************************************************************'
	
	println "Aquaman analysis finished: " + summaryVar
 
}


void getAnalysisDetails(patchId) {
   //Llaamos la web api que nos da el detalle del analisis, si no pasamos el parametro, nos devuelve unicamente los que han fallado
  String newUrl = SITEBASEURL + "/webapi/getPatchAnalysisDetails?id=" + "/" + patchId +"/"
  String analysisStatus = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
  analysisStatus = analysisStatus
  //.readLines().drop(1).join(" ")
  analysisStatusJson = new groovy.json.JsonSlurperClassic().parseText(analysisStatus)
  
  //Imprimimos los resultados para que los vea el usuario desde el pipeline
  println analysisStatusJson
}

void requestApplicationTests() {
	println "requestApplicationTests"
	
	//URL de la web api que usaremos para ejecutar los tests asociados a la aplicacion
   testUrl = SITEBASEURL + "/webapi/executeApplicationTests?application=$APPLICATIONNAME" 
   response=sh( script:"curl --location  --request POST \"$testUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
   println "Respuesta recibida"
    println response
   //.readLines().drop(1).join(" ")
   analysisStatusJson = new groovy.json.JsonSlurperClassic().parseText(response)
   testId = analysisStatusJson.TestRunId
   
   checkTestStatus(testId)
  
  
  println "Tests requested"
}

void checkTestStatus(testId) {
  sleep 15
  String newUrl = SITEBASEURL + "/webapi/getTestStatus?id=" + "/" + testId +"/"
  String analysisStatus = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
  analysisStatus = analysisStatus
  //.readLines().drop(1).join(" ")
  analysisStatusJson = new groovy.json.JsonSlurperClassic().parseText(analysisStatus)
  statusVar = analysisStatusJson.status
  println statusVar

  while (!statusVar.equals("COMPLETE")) {
    sleep 30
	println "statusVar:" + statusVar
	println statusVar.equals("COMPLETE")
    analysisStatus = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
	analysisStatusJson = new groovy.json.JsonSlurperClassic().parseText(analysisStatus)
    println analysisStatusJson
	statusVar = analysisStatusJson.status
	
  }
  
  //Llamamos al metodo que nos da detalle del resultado del test
  getTestDetails(testId)
  
  
}


void getTestDetails(testId) {
   //Llaamos la web api que nos da el detalle del analisis, si no pasamos el parametro, nos devuelve unicamente los que han fallado
  String newUrl = SITEBASEURL + "/webapi/getTestResults?id=" + "/" + testId +"/"
  String analysisStatus = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
  analysisStatus = analysisStatus
  //.readLines().drop(1).join(" ")
  analysisStatusJson = new groovy.json.JsonSlurperClassic().parseText(analysisStatus)
  
   summaryVar = analysisStatusJson.status
  
  if(summaryVar.equals("FAIL")||summaryVar.equals("ERROR")){
	
	//Devolvemos codigo de error dado que ha fallado el analisis en Aquaman para abortar el resto de la ejecucion
	error("Tests failed")
  }
  //Mostramos los resultados de los tests
	
	//En vez de esto, mostramos un link a un reporte con la informacion para el usuario
	println '************************************************************************************************************'
	println 'Haga click a continuacion para ver los resultados de la ejecucion de los tests'
	println 'https://everisspaindemo.appianportals.com/dc6d9daf-ab46-4a19-8db3-4769d03bc59a-portalAQ?patchId=' + testId
	println '************************************************************************************************************'
	
	println "Application tests finished: " + summaryVar
}


void checkDeploymentStatus() {
  sleep 15
  String newUrl = SITEBASEURL + "/deployment-management/v1/deployments" + "/" + deploymentResponseJson.uuid +"/"
  String deploymentStatus = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
  deploymentStatus = deploymentStatus
  //.readLines().drop(1).join(" ")
  deploymentStatusJson = new groovy.json.JsonSlurperClassic().parseText(deploymentStatus)
  statusVar = deploymentStatusJson.status
  while (statusVar.equals("IN_PROGRESS")) {
    sleep 30
    deploymentStatus = sh(script: "curl --silent --location --request GET \"$newUrl\" --header \"Appian-API-Key: $APIKEY\"" , returnStdout: true).trim()
    deploymentStatus = deploymentStatus.readLines().drop(1).join(" ")
    deploymentStatusJson = new groovy.json.JsonSlurperClassic().parseText(deploymentStatus)
    statusVar = deploymentStatusJson.status
  }
  println "Deployment Finished and Status is " + statusVar

}

void setProperty(filePath, property, propertyValue) {
  shNoTrace("sed -i -e 's|.\\?${property}=.*|${property}=${propertyValue}|' ${filePath}")
}

def shNoTrace(cmd) {
  sh '#!/bin/sh -e\n' + cmd
}

return this
