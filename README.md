# commits_viewer

This tool allows us to list all the commits for a given URL.

## How-To: Build the application artefact

Requires: Git, SBT and Scala to be installed

Before running the application, it needs to be built. This project has been implemented in Scala, and using SBT, so one can use
the following SBT to build the application artefact:
```
$> sbt assembly
```
This will generate a fat jar with everything that is needed to run the applicaiton in `dist/commits-viewer.jar`.

## How-To: Run the application

### Configuration

I used Typesafe Config for all the application configuration. In particular, I added the ability to change some configuration items by
providing environment variables that will override the corresponding defaults:
```

http {
  requests {
    timeout = 60 seconds
    timeout = ${?HTTP_REQUESTS_TIMEOUT}
  }
}

jvm {
  clean_resources_on_shutdown = true
  clean_resources_on_shutdown = ${?CLEAN_RESOURCES_SHUTDOWN}
}

git {
  api {
    token = ${?GITHUB_TOKEN}
  }

  cached_repos.dir = "/tmp/cached_repos"
  cached_repos.dir = ${?CACHED_REPOS_DIR}

  commits.list.default_limit = 10
  commits.list.default_limit = ${?COMMITS_LIST_LIMIT}
```

### HTTP Server

This application ships with an embedded HTTP server that will be listening on `localhost` and port `8888`. To run the application,
I have included a simple bash script in the same `dist` folder that can be executed once the previous step is completed:
```
$> ./run_server.sh
```

As soon as it starts, we should see this message in the logs:
```
23:33:24.308 [commit_viewer-akka.actor.default-dispatcher-2] INFO com.acme.commitviewer.http.CommitViewer$ - Server listening at 127.0.0.1:8888
```

Here's an example of a curl request executed against a running application:
```
curl http://localhost:8888/git/commits\?repository_url\=https://github.com/sbrnunes/commits_viewer.git\&limit\=10\&page\=1 | json_pp
```

#### Pagination

This API is paginated. By default, the results are fetched with a limit of 10 commits, starting on the most recent commit. By changing
the HTTP query parameters `limit` and `page` we can fetch a specific set of commits. Pages start at `1`, which is also the default.

#### Github API and CLI fallback

The application will first try to use the Github API but, in the case of a failure, it will try to recover and use the CLI as a fallback.
This CLI needs to clone and cache the repository in the local filesystem before attempting to fetch the commits. 

By default, it will use directory `/tmp/cached_repos` as the root directory for all cached repositories. This can be changed through an
environment variable.

To see this mechanism working, one can force the API request to fail, by providing a wrong Github API token:
```
$> GITHUB_TOKEN=abc ./run_server.sh
```

#### Long running requests

Some of this requests can potentially take some time:
- The number of commits requested may be too big
- The API call might fail and the application will need to fallback to the CLI, which may need to clone the entire repository before 
serving the request

There is a default timeout of 60s on each request. If this timeout expires, the following message will be returned:
```
{
   "msg" : "Request is taking too long to complete. Please try again in a few moments."
}
```

Suggested actions:
- Reduce the number of commits requested
- Repeat the request a few seconds later (the repository should have been cached, next requests should be served much faster)


### CLI

There's also the option to run the application as a CLI, passing the `repo`, `limit` and `page` as arguments:
```
$> ./run_cli.sh --repo https://github.com/sbrnunes/commits_viewer.git --limit 1 --page 1
```
