#!/usr/bin/env groovy

void releaseApplication(tag, customProperties) {
  def releaseName = tagSuccessfulImport(tag)
  def remoteURL = getRemoteRepo("appian/applications/${APPLICATIONNAME}").split("/")
  def remoteRepo = remoteURL[-1].trim().minus(".git")
  def remoteOwner = remoteURL[-2].trim()

  batNoTrace("curl --user \"${REPOUSERNAME}:${REPOPASSWORD}\" -X GET https://api.github.com/repos/${remoteOwner}/${remoteRepo}/releases/tags/${releaseName} > releaseGetResponse.json")
  def previousURL = bat script: "jq \'.url\' releaseGetResponse.json", returnStdout: true
  previousURL = previousURL.trim()
  if (previousURL != "null") {
    batNoTrace("curl --user \"${REPOUSERNAME}:${REPOPASSWORD}\" -X DELETE ${previousURL}")
  }

  def body = "${APPLICATIONNAME} has passed integration and acceptance tests and is being released."
  batNoTrace("curl --user \"${REPOUSERNAME}:${REPOPASSWORD}\" -d \'{\"tag_name\":\"${releaseName}\", \"name\":\"${releaseName}\", \"body\":\"${body}\", \"draft\":false, \"prerelease\":false}\' -X POST https://api.github.com/repos/${remoteOwner}/${remoteRepo}/releases > releasePostResponse.json")

  def uploadURL = bat script: "jq \'.upload_url\' releasePostResponse.json", returnStdout: true
  uploadURL = uploadURL.trim().minus("{?name,label}")
  batNoTrace("curl -s --user \"${REPOUSERNAME}:${REPOPASSWORD}\" --header \"Content-Type:application/zip\" --data-binary \"@adm/app-package.zip\" -X POST ${uploadURL}?name=${releaseName}.zip")
  if (fileExists("appian/properties/${APPLICATIONNAME}/" + customProperties)) {
    batNoTrace("curl -s --user \"${REPOUSERNAME}:${REPOPASSWORD}\" --header \"Content-Type:application/text\" --data-binary \"@appian/properties/${APPLICATIONNAME}/${customProperties}\" -X POST ${uploadURL}?name=${releaseName}.properties")
  }
}

void tagSuccessfulImport(tag) {
  def releaseName = "${APPLICATIONNAME}_${tag}"
  dir("appian/applications/${APPLICATIONNAME}") {
    def currentCommit = bat script: "git log -n 1 --format='%h' ./", returnStdout: true
    def remoteURL = bat script: "git config --get remote.origin.url", returnStdout: true
    remoteURL = "https://${REPOUSERNAME}:${REPOPASSWORD}@" + remoteURL.split("https://")[1]
    bat "git checkout master"
    bat "git tag -af ${releaseName} -m 'The application ${APPLICATIONNAME} has successfully been imported into ${tag}' ${currentCommit}"
    batNoTrace("git pubat -f --follow-tags --repo=${remoteURL}")
  }
  return releaseName
}

void getRemoteRepo(path) {
  dir("${path}") {
    def remoteURL = bat script: "git config --get remote.origin.url", returnStdout: true
    return remoteURL
  }
}

def batNoTrace(cmd) {
  bat '#!/bin/bat -e\n' + cmd
}

return this
