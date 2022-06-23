window['INJECTED_ENVIRONMENT'] = {
  OIDC_DISABLED: true,
  API_BASEURL: 'http://localhost:7171/api',
  DNA_COMPANY_NAME: 'Company_Name',
  DNA_APPNAME_HEADER: 'DnA App',
  DNA_APPNAME_HOME: 'Data and Analytics',
  DNA_CONTACTUS_HTML:
    '<div><p>There could be many places where you may need our help, and we are happy to support you. <br /> Please add your communication channels links here</p></div>',
  DNA_BRAND_LOGO_URL: '/images/branding/logo-brand.png',
  DNA_APP_LOGO_URL: '/images/branding/logo-app.png',
  OIDC_PROVIDER: 'OKTA',
  CLIENT_IDS: 'YOUR_CLIENT_ID',
  REDIRECT_URLS: 'YOUR_OKTA_REDIRECT_URL',
  OAUTH2_AUTH_URL: 'https://YOUR_OKTA_DOMAIN.okta.com/oauth2/v1/authorize',
  OAUTH2_LOGOUT_URL: 'https://YOUR_OKTA_DOMAIN.okta.com/oauth2/v1/logout',
  OAUTH2_REVOKE_URL: 'https://YOUR_OKTA_DOMAIN.okta.com/oauth2/v1/revoke',
  OAUTH2_TOKEN_URL: 'https://YOUR_OKTA_DOMAIN.okta.com/oauth2/v1/token',
  ENABLE_INTERNAL_USER_INFO: false,
  ENABLE_DATA_COMPLIANCE: false,
  ENABLE_JUPYTER_WORKSPACE: false,
  JUPYTER_NOTEBOOK_URL: 'YOUR_NOTEBOOK_URL',
  JUPYTER_NOTEBOOK_OIDC_POPUP_URL: 'YOUR_NOTEBOOK_AUTH_REDIRECT_URL',
  JUPYTER_NOTEBOOK_OIDC_POPUP_WAIT_TIME: 5000,
  ENABLE_DATAIKU_WORKSPACE: false,
  DATAIKU_LIVE_APP_URL: 'YOUR_DATAIKU_LIVE_URL',
  DATAIKU_TRAINING_APP_URL: 'YOUR_DATAIKU_TRAINING_URL',
  DATAIKU_FERRET_URL: 'YOUR_DATAIKU_FERRET_URL',
  ENABLE_MALWARE_SCAN_SERVICE: true,
  MALWARE_SCAN_SWAGGER_UI_URL: 'YOUR_MALWARE_SCAN_SWAGGER_UI_URL',
  ENABLE_MALWARE_SCAN_ONEAPI_INFO: false,
  ENABLE_DATA_PIPELINE_SERVICE: true,
  ENABLE_STORAGE_SERVICE: true,
  STORAGE_MFE_APP_URL: 'http://localhost:8083',
  ENABLE_REPORTS: true,
  ENABLE_ML_PIPELINE_SERVICE: true,
  ENABLE_NOTIFICATION: true,
  ML_PIPELINE_URL: 'YOUR_ML_PIPELINE_URL',
  MODEL_REGISTRY_API_BASEURL: 'YOUR_MODEL_REGISTRY_API_BASEURL',
  INTERNAL_USER_TEAMS_INFO:
    '(Recommended to use Short ID. To find Short ID use <a href="YOUR_TEAMS_INFO_URL" target="_blank" rel="noreferrer noopener">Teams</a>)',
};

window['STORAGE_INJECTED_ENVIRONMENT'] = {
  CONTAINER_APP_URL: 'http://localhost:9090',
  API_BASEURL: 'http://localhost:7171/api',
  STORAGE_API_BASEURL: 'http://localhost:7175/api',
  TOU_HTML: '<div>I agree to <a href="#" target="_blank" rel="noopener noreferrer">terms of use</a></div>',
  ENABLE_DATA_CLASSIFICATION_SECRET: false,
  TRINO_API_BASEURL: 'http://localhost:7575/api',
};
