const getInjectedEnv = (key) => {
    if (window.CODE_SPACE_INJECTED_ENVIRONMENT) {
      return window.CODE_SPACE_INJECTED_ENVIRONMENT[key];
    }
    return undefined;
  };
  
  // const getDNAInjectedEnv = (key) => {
  //   if (window.INJECTED_ENVIRONMENT) {
  //     return window.INJECTED_ENVIRONMENT[key];
  //   }
  //   return undefined;
  // };
  
  export const Envs = {
    CODE_SPACE_API_BASEURL: getInjectedEnv('CODE_SPACE_API_BASEURL') || process.env.CODE_SPACE_API_BASEURL,
    API_BASEURL: getInjectedEnv('API_BASEURL') || process.env.API_BASEURL,
    CODE_SPACE_GIT_PAT_APP_URL: getInjectedEnv('CODE_SPACE_GIT_PAT_APP_URL') || process.env.CODE_SPACE_GIT_PAT_APP_URL,
    CODE_SPACE_GIT_ORG_NAME: getInjectedEnv('CODE_SPACE_GIT_ORG_NAME') || process.env.CODE_SPACE_GIT_ORG_NAME,
    CODESPACE_OPENSEARCH_LOGS_URL: getInjectedEnv('CODESPACE_OPENSEARCH_LOGS_URL') || process.env.CODESPACE_OPENSEARCH_LOGS_URL,
    CODESPACE_OPENSEARCH_BUILD_LOGS_URL: getInjectedEnv('CODESPACE_OPENSEARCH_BUILD_LOGS_URL') || process.env.CODESPACE_OPENSEARCH_BUILD_LOGS_URL,
    CODESPACE_OIDC_POPUP_URL: getInjectedEnv('CODESPACE_OIDC_POPUP_URL') || process.env.CODESPACE_OIDC_POPUP_URL,
    CODESPACE_AWS_POPUP_URL: getInjectedEnv('CODESPACE_AWS_POPUP_URL') || process.env.CODESPACE_AWS_POPUP_URL,
    CODESPACE_OIDC_POPUP_WAIT_TIME: getInjectedEnv('CODESPACE_OIDC_POPUP_WAIT_TIME') || process.env.CODESPACE_OIDC_POPUP_WAIT_TIME,
    REPORTS_API_BASEURL: getInjectedEnv('REPORTS_API_BASEURL') || process.env.REPORTS_API_BASEURL,
    STORAGE_API_BASEURL: getInjectedEnv('STORAGE_API_BASEURL') || process.env.STORAGE_API_BASEURL,
    VAULT_API_BASEURL: getInjectedEnv('VAULT_API_BASEURL') || process.env.VAULT_API_BASEURL,
    CODESPACE_HARDWARE_REQUEST_TEMPLATE: getInjectedEnv('CODESPACE_HARDWARE_REQUEST_TEMPLATE') || process.env.CODESPACE_HARDWARE_REQUEST_TEMPLATE,
    CODESPACE_SOFTWARE_REQUEST_TEMPLATE:getInjectedEnv('CODESPACE_SOFTWARE_REQUEST_TEMPLATE') || process.env.CODESPACE_SOFTWARE_REQUEST_TEMPLATE,
    CODESPACE_TUTORIALS_BASE_URL:getInjectedEnv('CODESPACE_TUTORIALS_BASE_URL') || process.env.CODESPACE_TUTORIALS_BASE_URL,
    CODESPACE_RECIEPES_ENABLE_README: getInjectedEnv('CODESPACE_RECIEPES_ENABLE_README') || process.env.CODESPACE_RECIEPES_ENABLE_README,
    SHOW_AWS_MIGRATION_WARNING: getInjectedEnv('SHOW_AWS_MIGRATION_WARNING') || process.env.SHOW_AWS_MIGRATION_WARNING,
    AWS_MIGRATION_WARNING_MODAL_CONTENT: getInjectedEnv('AWS_MIGRATION_WARNING_MODAL_CONTENT') || process.env.AWS_MIGRATION_WARNING_MODAL_CONTENT,
  };