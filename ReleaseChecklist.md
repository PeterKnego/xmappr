## Things to do when releasing ##

_This is here just to remind the forgetful admins_

  1. increment the release number in pom.xml!!!
  1. make sure all code is commited to svn repo (check changelists in Idea)
  1. all tests must pass!
  1. build & release: mvn deploy
  1. create svn tag for release: svn copy https://xmappr.googlecode.com/svn/trunk/ https://xmappr.googlecode.com/svn/tags/x.y.z --username peter@knego.net -m "release x.y.z"

optional: build step will create javadoc in trunk/apidocs - commit it to svn