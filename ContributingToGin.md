# Contributing to GIN #

Thanks for your interest in contributing to GIN! In general, the process is similar to [GWT's extremely well-documented process](http://code.google.com/webtoolkit/makinggwtbetter.html#contributingcode).

## Code style ##

GIN uses the [same code style rules as GWT](http://code.google.com/webtoolkit/makinggwtbetter.html#codestyle), except that lines are allowed to be 100 characters long.

## Testing ##

Gin contains two groups of tests, in `test/com/google/gwt/inject/rebind` (rebind) and `test/com/google/gwt/inject/client` (client). The rebind tests exercise our generator directly, the client tests are `GWTTestCase`s that use the GWT compiler to run the generator (just as any normal Gin application would do) and test the results. The client tests are run twice in the build file, once in hosted/dev mode and once in web/production mode to make sure that no problems come up in either environment.

_Note:_ Since the rebind tests depend on GWT compiler internals that frequently change between releases, those tests usually work only with a specific GWT version. We try to keep them updated to the latest official GWT release but if you are trying to send in a patch and they're not up-to-date feel free to fix them.

Any code change has to include client tests and for complex calculations also rebind tests.

## Submitting a patch ##
This process applies for committers and non-committers alike.

_Prerequisites:_
  * Make sure that you're a member of the google-gin mailing list.
  * Submit a [contributor license agreement](http://code.google.com/webtoolkit/makinggwtbetter.html#clas), if you have not done so before (you can do this electronically).

_Process:_
  1. Create an issue in the GIN issue tracker, if needed. For simple internal changes, this step can be omitted.
  1. Using http://codereview.appspot.com, submit your patch for review to _aragos@gmail.com_, cc: _google-gin@googlegroups.com_ and add a comment to your issue that contains the review url.
  1. Once the review is complete (ending in an LGTM):
    * If you are a committer, submit.
    * If you are not a committer, ask a committer to submit your updated patch.
  1. Close the issue with a reference to the commit.