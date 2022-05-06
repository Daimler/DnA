#### **Environment Variables**

##### Frontend Environment Variables


| Name                                                          | Default Value                                                                                                | Options       | Description                                                                                                                                                                                                     |
| --------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- | --------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| PROJECTSMO_FRONTEND_OIDC_DISABLED<br />`boolean`              | true                                                                                                         | true/false    | To enable the OAuth Authentication make the value`false`                                                                                                                                                        |
| PROJECTSMO_FRONTEND_API_BASEURL<br />`url`                    | `http://<YOUR_HOST_URL>/api`                                                                                 | NA            | This is the base url for calling backend apis from frontend(ex: If frontend and backend runs on same host like`<YOUR_HOST_URL>` then backend api will defaultrun on the url path `http://<YOUR_HOST_URL>/api` . |
| PROJECTSMO_FRONTEND_OAUTH2_TOKEN_URL<br />`url`               | `<<https://dev-xxxxx.okta.com/oauth2/v1/token>>`                                                             | NA            | Get access toke api. for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).                                                                                                           |
| PROJECTSMO_FRONTEND_OAUTH2_AUTH_URL<br />`url`                | `<<https://dev-xxxxx.okta.com/oauth2/v1/authorize>>`                                                         | NA            | OIDC auth api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/)                                                                                                                   |
| PROJECTSMO_FRONTEND_OAUTH2_REVOKE_URL<br />`url`              | `<<https://dev-xxxxx.okta.com/oauth2/v1/revoke>>`                                                            | NA            | Revoke token api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).                                                                                                               |
| ROJECTSMO_FRONTEND_OAUTH2_LOGOUT_URL<br />`url`               | `<<https://dev-XXXX.okta.com/oauth2/v1/logout>>`                                                             | NA            | LOGOUT api. for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).                                                                                                                    |
| PROJECTSMO_FRONTEND_CLIENT_IDS<br />`secret`                  | `<<Client Id>>`                                                                                              | NA            | Client id of OIDC provider.                                                                                                                                                                                     |
| PROJECTSMO_FRONTEND_REDIRECT_URLS<br />`url`                  | `<<http://localhost:9090>>`                                                                                  | NA            | URL to be redirected after successful login.                                                                                                                                                                    |
| PROJECTSMO_FRONTEND_OIDC_PROVIDER<br />`string`               | `OKTA`                                                                                                       | OKTA/INTERNAL | Name of ODIC provider                                                                                                                                                                                           |
| PROJECTSMO_DNA_APPNAME_HEADER<br />`string`                   | `"DnA App"`                                                                                                  | NA            | APP name that comes on application header.                                                                                                                                                                      |
| PROJECTSMO_DNA_APPNAME_HOME<br />`string`                     | `"Data and Analytics "`                                                                                      | NA            | APP name that comes on home page.                                                                                                                                                                               |
| PROJECTSMO_DNA_CONTACTUS_HTML<br />`string`                   | `<div><p>There could be many places where you may need our help, and we are happy to support you. <p></div>` | NA            | Content that comes on contact us model.                                                                                                                                                                         |
| PROJECTSMO_DNA_BRAND_LOGO_URL<br />`url`                      | `/images/branding/logo-brand.png`                                                                            | NA            | Brand logo image that comes on the application header left corner.                                                                                                                                              |
| PROJECTSMO_DNA_APP_LOGO_URL<br />`url`                        | `/images/branding/logo-app.png`                                                                              |               | App logo image that comes on the application header right side.                                                                                                                                                 |
| PROJECTSMO_STORAGE_MFE_APP_URL<br />`url`                     | `<<http://localhost:8083>>`                                                                                  | NA            | Storage Micro Frontend Application URL, needed for Enabling Storage Service.                                                                                                                                    |
| PROJECTSMO_DNA_COMPANY_NAME<br/>`string`                      | `"Company_Name"`                                                                                             | NA            | Company name that comes on content of the application.                                                                                                                                                          |
| PROJECTSMO_DEPLOY_VERSION<br/>`number`                        | `"0.91"`                                                                                                     | NA            | Version of deployment.                                                                                                                                                                                          |
| PROJECTSMO_JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME<br/>`number` | `"5000"`                                                                                                     | NA            | OIDC popup wait time.                                                                                                                                                                                           |
| **PROJECTSMO_ENABLE_INTERNAL_USER_INFO<br/>`boolean`          | `false`                                                                                                      | true/false    | Only applicable if`OIDC_PROVIDER`is `INTERNAL`.                                                                                                                                                                 |
| **PROJECTSMO_ENABLE_DATA_COMPLIANCE<br/>`boolean`             | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **PROJECTSMO_ENABLE_JUPYTER_WORKSPACE<br/>`boolean`           | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **PROJECTSMO_ENABLE_DATAIKU_WORKSPACE<br/>`boolean`           | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **PROJECTSMO_ENABLE_MALWARE_SCAN_SERVICE<br/>`boolean`        | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **PROJECTSMO_ENABLE_DATA_PIPELINE_SERVICE<br/>`boolean`       | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **PROJECTSMO_ENABLE_ML_PIPELINE_SERVICE<br/>`boolean`         | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **PROJECTSMO_ENABLE_MALWARE_SCAN_ONEAPI_INFO<br/>`boolean`    | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **PROJECTSMO_ENABLE_NOTIFICATION<br/>`boolean`                | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **PROJECTSMO_ENABLE_STORAGE_SERVICE<br/>`boolean`             | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |
| **PROJECTSMO_ENABLE_REPORTS<br/>`boolean`                     | `false`                                                                                                      | true/false    | These features are coming soon. Changing/updating may result in unexpected behavior                                                                                                                             |

##### Backend Environment Variables


| Name                                      | Deafult Value                                            | Options             | Description                                                                                           |
| :------------------------------------------ | ---------------------------------------------------------- | --------------------- | ------------------------------------------------------------------------------------------------------- |
| API_DB_URL<br />`url`                     | `<<jdbc:postgresql://localhost:5432/db>>`                | NA                  | Database host address                                                                                 |
| API_DB_USER<br />`string`                 | `<<User id>>`                                            |                     | Database user name                                                                                    |
| API_DB_PASS<br />`string`                 | `<<password>>`                                           |                     | Database password                                                                                     |
| OIDC_DISABLED<br />`boolean`              | `true`                                                   | true/false          | To enable the OAuth Authentication make the value`false`                                              |
| OIDC_ISSUER`url`                          | `<<https://dev-xxxxx.okta.com/oauth2/default>>`          | NA                  | OIDC issuer url.                                                                                      |
| REDIRECT_URL<br />`url`                   | `<<http://localhost:9090>>`                              | NA                  | URL to be redirected after successful login.                                                          |
| OIDC_CLIENT_SECRET<br />`secret`          | `<<Client Secret>>`                                      | NA                  | Client secret of OIDC provider.                                                                       |
| OIDC_CLIENT_ID<br />`secret`              | `<<Client Id>>`                                          | NA                  | Client id of OIDC provider.                                                                           |
| OIDC_USER_INFO_URL<br />`url`             | `<<https://dev-xxxxx.okta.com/oauth2/v1/userinfo>>`      | NA                  | User Info api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).        |
| OIDC_TOKEN_INTROSPECTION_URL<br />`url`   | `<<https://dev-xxxxx.okta.com/oauth2/v1/introspect>>`    | NA                  | Token introspect api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/). |
| OIDC_TOKEN_REVOCATION_URL<br />`url`      | `<<https://dev-xxxxx.okta.com/oauth2/v1/revoke>>`        | NA                  | Revoke token api for[more](https://developer.okta.com/docs/reference/api/oidc/#endpointshttps:/).     |
| OIDC_PROVIDER<br />`string`               | `OKTA`                                                   | <br />OKTA/INTERNAL | Name of ODIC provider.                                                                                |
| INACTIVE_SOLUTION_DURATION_YRS<br />`int` | `2`                                                      | NA                  | Solution will be deleted if inactive for`value` configured.                                           |
| *S3_EP_URL<br />`url`                     | `https://s3-xxxx.com:443`                                | NA                  | S3 bucket url.                                                                                        |
| *S3_ACCESS_KEY<br />`secret`              | `<<access_key>>`                                         | NA                  | S3 access key.                                                                                        |
| *S3_BUCKET_NAME<br />`string`             | `Bucket_name`                                            | NA                  | S3 bucket name.                                                                                       |
| *S3_SECRET_KEY<br />`secret`              | `<<secret_key>>`                                         |                     | S3 secret key.                                                                                        |
| *S3_MAX_PARALLEL_UPLOADTHREADS<br />`int` | `20`                                                     | NA                  | To restrct no of parallel thread to upload file ot S3                                                 |
| *S3_MIN_FILESIZE<br />`int`               | `1024`                                                   | NA                  |                                                                                                       |
| *S3_MAX_FILESIZE<br />`int`               | `5242880`                                                | NA                  |                                                                                                       |
| CORS_ORIGIN_URL<br />`url pattern`        | `http://*`                                               | NA                  | CORS origin url restriction patterm.                                                                  |
| **JUPYTER_NOTEBOOK`boolean`               | `false`                                                  | true/false          | To enable jupyter notebook feature.                                                                   |
| **DATAIKU`boolean`                        | `false`                                                  | true/false          | To enable Dataiku feature.                                                                            |
| **ITSMM`boolean`                          | `false`                                                  | true/false          | To enable itsmm notebook feature.                                                                     |
| **ATTACHMENT_MALWARE_SCAN`boolean`        | `false`                                                  | true/false          | To enable file attachment scan before upload.                                                         |
| USER_ROLE`string`                         | `Admin`                                                  | User, Admin         | Available role is [`User`,`Admin`]                                                                    |
| BYPASS_JWT_AUTHENTICATION`string`         | `/api/login;/api/verifyLogin;/api/subscription/validate` |                     | URL to bypass token verification.                                                                     |
| JWT_TOKEN_EXPIRY_TIME_IN_MIN`int`         | `90`                                                     | NA                  | Set jwt token exipry                                                                                  |
| **INTERNAL_USER_REQUEST_URL`url`          |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **INTERNAL_USER_CERT_FILE`string`         |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **INTERNAL_USER_CERT_PASS`string`         |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **JUPYTER_NOTEBOOK_BASEURI`url`           |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **JUPYTER_NOTEBOOK_TOKEN`string`          |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **MATOMO_SITE_ID`string`                  |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **MATOMO_HOST_URL`url`                    |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_PROD_URI`url`                   |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_PROD_API_KEY`string`            |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_PROD_ADMIN_GROUP`url`           |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_TRAINING_URI`url`               |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_TRAINING_API_KEY`url`           |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **DATAIKU_TRAINING_ADMIN_GROUP`url`       |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **AVSCAN_URI`url`                         |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **AVSCAN_APP_ID`url`                      |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| **AVSCAN_API_KEY`url`                     |                                                          | NA                  | These features are coming soon. Changing/updating may result in unexpected behavior                   |
| *FLYWAY_ENABLED`boolean`                   | `true`                                                  | `true/false`          | To enable flyway                                                                                      |
| FLYWAY_BASELINE_ON_MIGRATE`boolean`       | `true`                                                  | `true/false`          | To enable flyway baseline migration                                                                   |
| FLYWAY_BASELINEVERSION`int`               | `0`                                                        | `0/1/2...`            | Flyway baseline version                                                                               |
| FLYWAY_SCHEMA`string`                     | `public`                                                   |                     | Flyway schema                                                                                         |
| *NAAS_BROKER`url`                          | `<<localhost:9092>>`                                           |                     | Notification service url                                                                              |
| *VAULT_HOST`url`                           | `localhost`                                                |                     | Host name of Hashicorp vault                                                                          |
| *VAULT_PORT`string`                        | `8200`                                                     |                     | Port number of Hashicorp vault                                                                        |
| *VAULT_SCHEME`string`                      | `http`                                                     |                     | Protocol to connect with Hashicorp vault                                                              |
| *VAULT_AUTHENTICATION`string`              | `TOKEN`                                                    |                     | Authentication method to connect with Hashicorp vault                                                 |
| *VAULT_TOKEN`string`                       | `***REMOVED***`                     |                     | Admin token to connect with Hashicorp. vault                                                           |
| *VAULT_MOUNTPATH`string`                   | `secret`                                                   |                     | Mount path to store secret in Hashicorp. vault                                                         |
| *VAULT_PATH`string`                        | `dna/avscan`                                               |                     | Path in which secrets to be stored in Hashicorp. vault                                                 |
| *DRD_INTERNAL_USER_ENABLED`boolean`        | `false`                                                    | `true/false`                  | To enable internal user                                                                               |
| *JWT_SECRET_KEY`string`                    | NA                                                       | NA                  | Default jwt secret key. key                                                                                |
| *DASHBOARD_URI`url`                        |                                                          | NA                  | Url to connect with dashboard service                                                                 |
| SWAGGER_HEADER_AUTH`string`               |                                                          | NA                  | Auth token for swagger ui                                                                             |
| *LOGGING_ENVIRONMENT`string`               | `DEV`                                                         | `DEV/PROD`            | Environment name                                                                                      |
| *LOGGING_PATH`string`                      | `/logs`                                                         | NA                  | Path for log file                                                                                     |

##### Airflow Backend Environment Variables


| Name                                   | Deafult Value                             | Options            | Description                                                   |
| :--------------------------------------- | ------------------------------------------- | -------------------- | --------------------------------------------------------------- |
| *FLYWAY_ENABLED`boolean`                | `true`                                   | `true/false`         | To enable flyway.                                              |
| FLYWAY_BASELINE_ON_MIGRATE`boolean`    | `true`                                   | `true/false`         | To enable flyway baseline migration.                           |
| FLYWAY_BASELINEVERSION`int`            | 0                                         | `0/1/2...`           | Flyway baseline version.                                       |
| FLYWAY_SCHEMA`string`                  | `public`                                    |                    | Flyway schema name.                                                 |
| *API_DB_URL<br />`url`                  | `<<jdbc:postgresql://localhost:5432/db>>` | NA                 | Database host address.                                         |
| *API_DB_USER<br />`string`              | `<<User id>>`                             |                    | Database user name.                                            |
| *API_DB_PASS<br />`string`              | `<<password>>`                            |                    | Database password.                                             |
| SWAGGER_HEADER_AUTH`string`            |                                           | NA                 | Auth token for swagger ui.                                     |
| *CORS_ORIGIN_URL<br />`url pattern`     | `http://*`                                | NA                 | CORS origin url restriction patterm.                          |
| *DNA_AUTH_ENABLE`boolean`               | `false`                                   | `true/false`         | To enable authorization from DnA backend.                     |
| DNA_URI`url`                           | `http://localhost:7171`                   | NA                 | DnA backend url to validate jwt token.                        |
| *JWT_SECRET_KEY`string`                 | NA                                        | NA                 | Default jwt secret key.                                        |
| *AIRFLOW_GIT_URI`url`                   | `https:git/dna/XXX`                       | NA                 | GIT url to push DAG for airflow.                              |
| *AIRFLOW_GIT_MOUNTPATH`string`          | `*\GITTest\airflow-user-dags`             | NA                 | Path to clone Airflow DAG repository.                         |
| *AIRFLOW_GIT_TOKEN`string`              | NA                                        | NA                 | Token to connect with Airflow DAG repository.                 |
| *AIRFLOW_GIT_BRANCH`string`             | `master`                                  | `master/development/any branch` | Airflow DAG repository branch name in which DAG to be pushed. |
| *DAG_PATH`string`                       | `dags`                                    | NA                 | Path inside airflow DAG repository where DAG to be pushed.    |
| *DAG_FILE_EXTENSION`string`             | `py`                                      | NA                 | DAG file extension example: for python py.                    |
| *AIRFLOW_DAG_MENU_CREATE_WAIT_TIME`int` | `4`                                       | NA                 | Wait time in second for airflow dag menu creation.            |
| *LOGGING_ENVIRONMENT`string`            | `DEV`                                          | `DEV/PROD/any environment name`           | Environment name.                                              |
| *LOGGING_PATH`string`                   | `/logs`                                          | NA                 | Path for log file.                                             |

##### Dashboard Backend Environment Variables


| Name                                | Deafult Value                             | Options    | Description                               |
| :------------------------------------ | ------------------------------------------- | ------------ | ------------------------------------------- |
| *FLYWAY_ENABLED`boolean`             | `true`                                   | `true/false` | To enable flyway.                          |
| FLYWAY_BASELINE_ON_MIGRATE`boolean` | `true`                                   | `true/false` | To enable flyway baseline migration.       |
| FLYWAY_BASELINEVERSION`int`         | `0`                                         | `0/1/2...`   | Flyway baseline version.                   |
| FLYWAY_SCHEMA`string`               | `public`                                    | `Schema_name`           | Flyway schema.                             |
| *API_DB_URL<br />`url`               | `<<jdbc:postgresql://localhost:5432/db>>` | NA         | Database host address.                     |
| *API_DB_USER<br />`string`           | `<<User id>>`                             | NA           | Database user name.                        |
| *API_DB_PASS<br />`string`           | `<<password>>`                            | NA           | Database password.                         |
| SWAGGER_HEADER_AUTH`string`         | NA                                          | NA         | Auth token for swagger ui.                 |
| *CORS_ORIGIN_URL<br />`url pattern`  | `http://*`                                | NA         | CORS origin url restriction patterm.      |
| *DNA_AUTH_ENABLE`boolean`            | `false`                                   | `true/false` | To enable authorization from DnA backend. |
| DNA_URI`url`                        | `http://localhost:7171`                   | NA         | DnA backend url to validate jwt token.    |
| JWT_SECRET_KEY`string`              | NA                                        | NA         | Default jwt secret key.                    |
| *LOGGING_ENVIRONMENT`string`         | `DEV`                                          | `DEV/PROD`   | Environment name.                         |
| *LOGGING_PATH`string`                | `/logs`                                          | NA         | Path for log file.                        |

##### Notification Backend Environment Variables


| Name                                       | Deafult Value                             | Options    | Description                                                    |
| :------------------------------------------- | ------------------------------------------- | ------------ | ---------------------------------------------------------------- |
| *API_DB_URL<br />`url`                      | `<<jdbc:postgresql://localhost:5432/db>>` | NA         | Database host address.                                          |
| *API_DB_USER<br />`string`                  | `<<User id>>`                             | NA           | Database user name.                                             |
| *API_DB_PASS<br />`string`                  | `<<password>>`                            | NA           | Database password.                                              |
| SWAGGER_HEADER_AUTH`string`                | NA                                          | NA         | Auth token to access swagger ui.                               |
| *DNA_MAIL_SERVER_HOST<br />`string`         | `<<localhost>>`                           | NA           | Mail server host name.                                         |
| *DNA_MAIL_SERVER_PORT<br />`string`         | `<<port>>`                                | NA           | Mail server port number.                                       |
| *NAAS_BROKER<br />`url`                     | `<<localhost:9092>>`                      | NA           | Kafka broker url.                                              |
| *NAAS_CENTRAL_TOPIC<br />`string`           | `dnaCentralEventTopic`                    | NA           | Kafka central topic where event to be published.               |
| *NAAS_CENTRALREAD_TOPIC<br />`string`       | `dnaCentralReadTopic`                     | NA           | Kafka central topic where read messages will be pushed.        |
| *NAAS_CENTRALDELTE_TOPIC<br />`string`      | `dnaCentralDeleteTopic`                   | NA           | Kafka central topic where deleted messages will be pushed.     |
| *POLL_TIME<br />`int`                       | `5000`                                    | NA         | waiting time in milliseconds for each poll.                    |
| *MAX_POLL_RECORDS<br />`int`                | `5000`                                    | NA         | maximum no of records needed to pulled in on poll.             |
| *CORS_ORIGIN_URL<br />`url pattern`         | `http://*`                                | NA         | CORS origin url restriction patterm.                           |
| *DNA_AUTH_ENABLE`boolean`                   | `false`                                   | `true/false` | To enable authorization from DnA backend.                      |
| *DNA_URI`url`                               | `http://localhost:7171`                   | NA         | DnA backend url to validate jwt token.                         |
| JWT_SECRET_KEY`string`                     | NA                                        | NA         | Default jwt secret key.                                         |
| *DNA_USER_NOTIFICATION_PREF_GET_URI`string` | /api/notification-preferences             | NA         | Url path to get user notification preference from DnA backend. |
| *DNA_NOTIFICATION_SENDER_EMAIL`string`      | `XXXXX@dna-XXXXX`                         | NA         | DnA notification sender email.                                 |
| *LOGGING_ENVIRONMENT`string`                | `DEV`                                          | `DEV/PROD`   | Environment name.                                              |
| *LOGGING_PATH`string`                       | `/logs`                                          | NA         | Path for log file.                                             |

##### Malware scanner Backend Environment Variables


| Name                               | Deafult Value                                         | Options           | Description                                                 |
| :----------------------------------- | ------------------------------------------------------- | ------------------- | ------------------------------------------------------------- |
| *CORS_ORIGIN_URL<br />`url pattern` | `http://*`                                            | NA                | CORS origin url restriction patterm.                        |
| *MAX_FILE_SIZE<br />`int`           | `10MB`                                                | `upto 3000MB`     | Maximum allowed file size.                                  |
| *MAX_REQUEST_SIZE<br />`int`        | `11MB`                                                | `upto 3000MB`     | Maximum allowed request size.                               |
| *CLAMAV_BACKEND_URL<br />`string`   | `localhost`                                           | NA                | CLAMAV url for file scan.                                   |
| *CLAMAV_BACKEND_PORT<br />`int`     | `3310`                                                | NA                | CLAMAV port.                                                |
| *AUTH_API_HOST`url`                 | `<<http://localhost:7171/api/subscription/validate>>` | NA                | url to validate subscription for the service.               |
| AUTH_API_TOKEN`string`             | NA                                                    | NA                | NA                                                          |
| ONEAPI_BASICAUTH_TOKEN`string`     | NA                                                    | NA                | Basic authorization token to allow connection from one API. |
| *RESTRICTED_URL_PATTERN`string`     | `/avscan/api/v1/scan.*`                               | NA                | Restricted url pattern.                                     |
| *API_REQUEST_LIMIT`int`             | `1`                                                   | `1,2,3...`        | Limit to allow number of request in given time span.        |
| *WITH_IN`int`                       | `20`                                                  | `any number`      | Time for which number of api request allowed.               |
| *TIME_UNIT`string`                  | `seconds`                                             | `seconds/minutes` | Time unit to restrict api request limit.                    |
| *LOGGING_ENVIRONMENT`string`        | `DEV`                                                      | DEV/PROD          | Environment name.                                           |
| *LOGGING_PATH`string`               | `/logs`                                                      | NA                | Path for log file.                                          |

##### Storage Backend Environment Variables


| Name                               | Deafult Value                        | Options       | Description                                           |
| :----------------------------------- | -------------------------------------- | --------------- | ------------------------------------------------------- |
| *CORS_ORIGIN_URL<br />`url pattern` | `http://*`                           | NA            | CORS origin url restriction patterm.                  |
| *MAX_FILE_SIZE<br />`int`           | `10MB`                               | `upto 3000MB` | Maximum allowed file size.                            |
| *MAX_REQUEST_SIZE<br />`int`        | `11MB`                               | `upto 3000MB` | Maximum allowed request size.                         |
| *VAULT_HOST`url`                    | localhost                            | NA              | Host name of Hashicorp vault.                          |
| *VAULT_PORT`string`                 | 8200                                 | NA              | Port number of Hashicorp vault.                        |
| *VAULT_SCHEME`string`               | http                                 | NA              | Protocol to connect with Hashicorp vault.              |
| *VAULT_AUTHENTICATION`string`       | TOKEN                                | NA              | Authentication method to connect with Hashicorp vault. |
| *VAULT_TOKEN`string`                | ***REMOVED*** | NA              | Admin token to connect with Hashicorp vault.           |
| *VAULT_MOUNTPATH`string`            | secret                               | NA              | Mount path to store secret in Hashicorp vault.         |
| *VAULT_PATH`string`                 | dna/storage                          | NA              | Path in which secrets to be stored in Hashicorp vault. |
| *DNA_AUTH_ENABLE`boolean`           | `false`                              | `true/false`    | To enable authorization from DnA backend.             |
| DNA_URI`url`                       | `http://localhost:7171`              | NA            | DnA backend url to validate jwt token.                |
| *JWT_SECRET_KEY`string`             | NA                                   | NA            | Default jwt secret key.                                |
| ATTACHMENT_MALWARE_SCAN`boolean`   | `false`                              | `true/false`    | To enable malware scan for attachments.               |
| MALWARE_SCANNER_APP_ID`string`     | NA                                   | NA            | Application ID of malware scan service subscription.  |
| MALWARE_SCANNER_API_KEY`string`    | NA                                   | NA            | API key of malware scan service subscription.         |
| MALWARE_SCANNER_URI`url`           | `<<http://localhost:7171>>`          | NA            | Malware scanner service url.                          |
| *MINIO_ENDPOINT`url`                | `<<http://localhost:9000>>`          | NA            | Minio endpoint url.                                   |
| *MINIO_ADMIN_ACCESS_KEY`string`     | NA                                   | NA            | Admin access key of Minio.                            |
| *MINIO_ADMIN_SECRET_KEY`string`     | NA                                   | NA            | Admin secret key of Minio.                            |
| *MINIO_POLICY_VERSION`string`       | NA                                   | NA            | Minio policy version.                                 |
| SWAGGER_HEADER_AUTH`string`        |  NA                                    | NA            | Auth token for swagger ui.                             |
| *LOGGING_ENVIRONMENT`string`        |  `DEV`                                    | `DEV/PROD/Any environment name`      | Environment name.                                      |
| *LOGGING_PATH`string`               |  `/logs`                                    | NA            | Path for log file.                                     |

<br />

**Note**

1. Marked `*`are mandatory.
2. Marked `**` These features are coming soon. Changing/updating may result in unexpected behavior.
