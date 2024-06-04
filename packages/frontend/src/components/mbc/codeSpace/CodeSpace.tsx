import React, { useState, useEffect } from 'react';
// @ts-ignore
import Notification from '../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../assets/modules/uilab/js/src/progress-indicator';
// @ts-ignore
import Tabs from '../../../assets/modules/uilab/js/src/tabs';

// @ts-ignore
import { Envs } from 'globals/Envs';
import { ICodeCollaborator, IUserInfo } from 'globals/types';
import { history } from '../../../router/History';
import { buildGitJobLogViewURL, buildGitUrl, buildLogViewURL, recipesMaster, trackEvent } from '../../../services/utils';
import Modal from 'components/formElements/modal/Modal';
import Styles from './CodeSpace.scss';
import FullScreenModeIcon from 'components/icons/fullScreenMode/FullScreenModeIcon';

// @ts-ignore
import Tooltip from '../../../assets/modules/uilab/js/src/tooltip';
import NewCodeSpace from './newCodeSpace/NewCodeSpace';
import ProgressWithMessage from 'components/progressWithMessage/ProgressWithMessage';
import { CodeSpaceApiClient } from '../../../services/CodeSpaceApiClient';
import { getParams } from '../../../router/RouterUtils';
import classNames from 'classnames';
import { CODE_SPACE_TITLE } from 'globals/constants';
import { DEPLOYMENT_DISABLED_RECIPE_IDS } from 'globals/constants';
import { IconGear } from 'components/icons/IconGear';
import VaultManagement from './vaultManagement/VaultManagement';
import DeployAuditLogsModal from './deployAuditLogsModal/DeployAuditLogsModal';
import DeployModal from './deployModal/DeployModal';

export interface ICodeSpaceProps {
  user: IUserInfo;
}

export interface IRecipeDetails {
  recipeId?: string;
  environment?: string;
  cloudServiceProvider?: string;
  ramSize?: string;
  cpuCapacity?: string;
  operatingSystem?: string;
}

export interface IProjectDetails {
  projectName?: string;
  projectOwner?: ICodeCollaborator;
  gitRepoName?: string,
  projectCreatedOn?: null,
  recipeDetails?: IRecipeDetails;
  projectCollaborators?: ICodeCollaborator[];
  intDeploymentDetails?: IDeploymentDetails;
  prodDeploymentDetails?: IDeploymentDetails;
  securityConfig?: any;
  publishedSecuirtyConfig?: any;
  dataGovernance?: IDataGovernance;
}

export interface IDataGovernance{
  description?: string;
  classificationType?: string;
  divisionId?: string;
  division?: string;
  subDivisionId?: string;
  subDivision?: string;
  department?: string;
  archerId?: string;
  procedureID?: string;
  tags?: string[];
  typeOfProject?: string;
  piiData?: boolean;

}

export interface IDeploymentAuditLogs{
  branch?: string;
  deployedOn?: string;
  triggeredBy?: string;
  triggeredOn?: string;
  deploymentStatus?: string;
}

export interface IDeploymentDetails {
  secureWithIAMRequired?: boolean,
  technicalUserDetailsForIAMLogin?: string,
  lastDeployedOn?: string;
  deploymentUrl?: string;
  lastDeployedBranch?: string;
  lastDeploymentStatus?: string;
  lastDeployedBy?: ICodeCollaborator;
  deploymentAuditLogs?: IDeploymentAuditLogs[];
  gitjobRunID?: string;
}

export interface ICodeSpaceData {
  id?: string;
  workspaceId?: string;
  description?: string,
  gitUserName?: string,
  intiatedOn?: string,
  workspaceUrl?: string,
  status?: string;
  configStatus?: string;
  workspaceOwner?: ICodeCollaborator,
  projectDetails?: IProjectDetails;
  running?: boolean;
  serverStatus?: string;
}

export interface IDeployRequest {
  targetEnvironment: string; // int or prod
  branch: string;
  secureWithIAMRequired?: boolean,
  technicalUserDetailsForIAMLogin?: string,
  valutInjectorEnable?: boolean,
}

let isTouch = false;

const CodeSpace = (props: ICodeSpaceProps) => {
  // const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
  //   url: `https://xxxx/${props.user.id.toLocaleLowerCase()}/default/?folder=/home/coder/projects/default/demo`,
  //   running: false
  // });
  const { id } = getParams();
  const [codeSpaceData, setCodeSpaceData] = useState<ICodeSpaceData>({
    workspaceUrl: undefined,
    running: false,
  });
  const [fullScreenMode, setFullScreenMode] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [showNewCodeSpaceModal, setShowNewCodeSpaceModal] = useState<boolean>(false);
  const [isApiCallTakeTime, setIsApiCallTakeTime] = useState<boolean>(false);
  const [showCodeDeployModal, setShowCodeDeployModal] = useState<boolean>(false);
  const [codeDeploying, setCodeDeploying] = useState<boolean>(false);
  const [codeDeployed, setCodeDeployed] = useState<boolean>(false);
  const [codeDeployedUrl, setCodeDeployedUrl] = useState<string>();
  const [codeDeployedBranch, setCodeDeployedBranch] = useState<string>('main');
  const [intCodeDeployFailed, setIntCodeDeployFailed] = useState<boolean>(false);
  const [prodCodeDeployed, setProdCodeDeployed] = useState<boolean>(false);
  const [prodCodeDeployedUrl, setProdCodeDeployedUrl] = useState<string>();
  const [prodCodeDeployedBranch, setProdCodeDeployedBranch] = useState<string>('main');
  const [prodCodeDeployFailed, setProdCodeDeployFailed] = useState<boolean>(false);
  const [livelinessInterval, setLivelinessInterval] = useState<number>();
  const [isStaging, setIsStaging] = useState(false);
  const [logsList, setlogsList] = useState([]);
  const [showVaultManagementModal, setShowVaultManagementModal] = useState(false);
  const [showAuditLogsModal, setShowAuditLogsModal] = useState(false);

  const [showContextMenu, setShowContextMenu] = useState<boolean>(false);
  const [contextMenuOffsetTop, setContextMenuOffsetTop] = useState<number>(0);
  const [contextMenuOffsetLeft, setContextMenuOffsetLeft] = useState<number>(0);

  const [serverStarted, setServerStarted] = useState(true);
  const [serverProgress, setServerProgress] = useState(0);

  const livelinessIntervalRef = React.useRef<number>();

  // const [branchValue, setBranchValue] = useState('main');
  // const [deployEnvironment, setDeployEnvironment] = useState('staging');
  const [showLogsView, setShowLogsView] = useState(false);

  const recipes = recipesMaster;
  

  const isAPIRecipe =
    codeSpaceData?.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
    codeSpaceData?.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
    codeSpaceData?.projectDetails?.recipeDetails?.recipeId === 'dash' ||
    codeSpaceData?.projectDetails?.recipeDetails?.recipeId === 'streamlit' ||
    codeSpaceData?.projectDetails?.recipeDetails?.recipeId === 'expressjs' ||
    codeSpaceData?.projectDetails?.recipeDetails?.recipeId === 'nestjs';

  const isIAMRecipe =
    codeSpaceData?.projectDetails?.recipeDetails?.recipeId === 'springboot' ||
    codeSpaceData?.projectDetails?.recipeDetails?.recipeId === 'py-fastapi' ||
    codeSpaceData?.projectDetails?.recipeDetails?.recipeId === 'expressjs';

  useEffect(() => {
    document.addEventListener('touchend', handleContextMenuOutside, true);
    document.addEventListener('click', handleContextMenuOutside, true);
    return () => {
      document.removeEventListener('touchend', handleContextMenuOutside, true);
      document.removeEventListener('click', handleContextMenuOutside, true);
    };
  }, []);

  const handleContextMenuOutside = (event: MouseEvent | TouchEvent) => {
    if (event.type === 'touchend') {
      isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && isTouch === true) {
      return;
    }

    const target = event.target as Element;
    const elemClasses = target.classList;
    const codespaceViewDivElement = document?.querySelector('#codespace-view');
    const contextMenuWrapper = codespaceViewDivElement?.querySelector('.contextMenuWrapper');

    if (
      codespaceViewDivElement &&
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      showContextMenu
    ) {
      setShowContextMenu(false);
    } else if (codespaceViewDivElement?.contains(target) === false) {
      setShowContextMenu(false);
    }

    if (!contextMenuWrapper?.contains(target)) {
      setShowContextMenu(false);
    }

    if (
      showContextMenu &&
      (elemClasses.contains('contextList') ||
        elemClasses.contains('contextListItem') ||
        elemClasses.contains('contextMenuWrapper') ||
        elemClasses.contains('locationsText'))
    ) {
      event.stopPropagation();
    }
  };

  const toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    setContextMenuOffsetTop(e.currentTarget.offsetTop - 17);
    setContextMenuOffsetLeft(e.currentTarget.offsetLeft - 230);
    setShowContextMenu(!showContextMenu);
  };

  useEffect(() => {
    if (id) {
      setLoading(true);
      CodeSpaceApiClient.getCodeSpaceStatus(id)
        .then((res: ICodeSpaceData) => {
          const serverStarted = res.serverStatus === 'SERVER_STARTED';
          setServerStarted(serverStarted);
          if (serverStarted) {
            handleOIDCLogin(res);
          } else {
            setLoading(true);
            CodeSpaceApiClient.startStopWorkSpace(res.id, false)
              .then((response: any) => {
                setLoading(false);
                if (response.success === 'SUCCESS') {
                  Notification.show(
                    'Your Codespace for project ' +
                      res.projectDetails?.projectName +
                      ' is requested to start.',
                  );

                  
                  CodeSpaceApiClient.serverStatusFromHub(props.user.id.toLowerCase(), res.workspaceId, (e: any) => {
                    const data = JSON.parse(e.data);
                    if (data.progress === 100 && data.ready) {
                      setServerProgress(100);
                      setTimeout(() => {
                        setServerStarted(true);
                        handleOIDCLogin(res);
                      }, 300);
                    } else if(!data.failed) {
                      setServerProgress(data.progress);
                    }
                    console.log(JSON.parse(e.data));
                  });

                } else {
                  Notification.show('Error in starting your code spaces. Please try again later.', 'alert');
                }
              })
              .catch((err: Error) => {
                setLoading(false);
                Notification.show(
                  'Error in ' + (serverStarted ? 'stopping' : 'starting') + ' your code spaces - ' + err.message,
                  'alert',
                );
              });
          }
        })
        .catch((err: Error) => {
          setLoading(false);
          Notification.show('Error in loading codespace - Please contact support.' + err.message, 'alert');
          history.replace('/codespaces');
        });
    } else {
      Notification.show('Codespace id is missing. Please choose your codespace to open.', 'warning');
      history.replace('/codespaces');
    }
  }, []);

  useEffect(() => {
    livelinessIntervalRef.current = livelinessInterval;
    return () => {
      livelinessIntervalRef.current && clearInterval(livelinessIntervalRef.current);
    };
  }, [livelinessInterval]);

  useEffect(() => {
    showLogsView && Tabs.defaultSetup();
  }, [showLogsView]);

  const handleOIDCLogin = (res: ICodeSpaceData) => {
    const loginWindow = window.open(
      Envs.CODESPACE_OIDC_POPUP_URL + `user/${props.user.id.toLowerCase()}/${res.workspaceId}/`,
      'codeSpaceSessionWindow',
      'width=100,height=100,location=no,menubar=no,status=no,titlebar=no,toolbar=no',
    );

    setTimeout(() => {
      loginWindow?.close();

      setLoading(false);
      const status = res.status;
      if (
        status !== 'CREATE_REQUESTED' &&
        status !== 'CREATE_FAILED' &&
        status !== 'DELETE_REQUESTED' &&
        status !== 'DELETED' &&
        status !== 'DELETE_FAILED'
      ) {
        const intDeploymentDetails = res.projectDetails.intDeploymentDetails;
        const prodDeploymentDetails = res.projectDetails.prodDeploymentDetails;
        const intDeployedUrl = intDeploymentDetails?.deploymentUrl;
        const prodDeployedUrl = prodDeploymentDetails?.deploymentUrl;
        const intDeployed =
          intDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
          (intDeployedUrl !== null && intDeployedUrl !== 'null');
        const intDeployFailed = intDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED';
        const prodDeployed =
          prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
          (prodDeployedUrl !== null && prodDeployedUrl !== 'null');
        const prodDeployFailed = prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYMENT_FAILED';
        const deployingInProgress =
          intDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED' ||
          prodDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED';
        // const deployed =
        //   intDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
        //   prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
        //   (intDeployedUrl !== null && intDeployedUrl !== 'null') ||
        //   (prodDeployedUrl !== null && prodDeployedUrl !== 'null');

        setCodeSpaceData({
          ...res,
          running: !!res.intiatedOn,
        });

        setCodeDeployedUrl(intDeployedUrl);
        setCodeDeployedBranch(intDeploymentDetails.lastDeployedBranch);
        setCodeDeployed(intDeployed);
        setIntCodeDeployFailed(intDeployFailed);

        setProdCodeDeployedUrl(prodDeployedUrl);
        setProdCodeDeployedBranch(prodDeploymentDetails.lastDeployedBranch);
        setProdCodeDeployed(prodDeployed);
        setProdCodeDeployFailed(prodDeployFailed);

        Tooltip.defaultSetup();
        Tabs.defaultSetup();
        if (deployingInProgress) {
          const deployingEnv =
            intDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED' ? 'staging' : 'production';
          // setDeployEnvironment(deployingEnv);
          setCodeDeploying(true);
          enableDeployLivelinessCheck(res.workspaceId, deployingEnv);
        }
      } else {
        Notification.show(
          `Code space ${res.projectDetails.projectName} is getting created. Please try again later.`,
          'warning',
        );
      }
    }, Envs.CODESPACE_OIDC_POPUP_WAIT_TIME);
  };

  const toggleFullScreenMode = () => {
    setFullScreenMode(!fullScreenMode);
    trackEvent(
      'DnA Code Space',
      'View Mode',
      'Code Space iframe view changed to ' + (fullScreenMode ? 'Full Screen Mode' : 'Normal Mode'),
    );
  };

  const openInNewtab = () => {
    window.open(codeSpaceData.workspaceUrl, '_blank');
    trackEvent('DnA Code Space', 'Code Space Open', 'Open in New Tab');
  };

  const toggleLogView = () => {
    setShowLogsView(!showLogsView);
  };

  const isCodeSpaceCreationSuccess = (status: boolean, codeSpaceData: ICodeSpaceData) => {
    setShowNewCodeSpaceModal(!status);
    setCodeSpaceData(codeSpaceData);
    Tooltip.defaultSetup();
  };

  const toggleProgressMessage = (show: boolean) => {
    setIsApiCallTakeTime(show);
  };

  const onNewCodeSpaceModalCancel = () => {
    setShowNewCodeSpaceModal(false);
    clearInterval(livelinessInterval);
    Tooltip.clear();
    history.goBack();
  };

  const onShowCodeDeployModal = () => {
    setShowCodeDeployModal(true);
  };

  const enableDeployLivelinessCheck = (id: string, deployEnvironmentValue: string) => {
    clearInterval(livelinessInterval);
    const intervalId = window.setInterval(() => {
      CodeSpaceApiClient.getCodeSpaceStatus(id)
        .then((res: ICodeSpaceData) => {
          try {
            const intDeploymentDetails = res.projectDetails?.intDeploymentDetails;
            const prodDeploymentDetails = res.projectDetails?.prodDeploymentDetails;

            const deployStatus =
              deployEnvironmentValue === 'staging'
                ? intDeploymentDetails?.lastDeploymentStatus
                : prodDeploymentDetails?.lastDeploymentStatus;
            if (deployStatus === 'DEPLOYED') {
              setIsApiCallTakeTime(false);
              ProgressIndicator.hide();
              clearInterval(livelinessIntervalRef.current);
              setCodeDeploying(false);
              if (deployEnvironmentValue === 'staging') {
                setCodeDeployedUrl(intDeploymentDetails?.deploymentUrl);
                setCodeDeployedBranch(intDeploymentDetails?.lastDeployedBranch);
                setCodeDeployed(true);
              } else if (deployEnvironmentValue === 'production') {
                setProdCodeDeployedUrl(prodDeploymentDetails?.deploymentUrl);
                setProdCodeDeployedBranch(prodDeploymentDetails?.lastDeployedBranch);
                setProdCodeDeployed(true);
              }
              Tabs.defaultSetup();
              Tooltip.defaultSetup();
              setShowCodeDeployModal(false);
              Notification.show(`Code from code space ${res.projectDetails?.projectName} succesfully deployed.`);
            }
            if (deployStatus === 'DEPLOYMENT_FAILED') {
              ProgressIndicator.hide();
              clearInterval(livelinessIntervalRef.current);
              setCodeDeploying(false);
              setShowCodeDeployModal(false);
              setIsApiCallTakeTime(false);
              Notification.show(
                `Deployment failed for code space ${res.projectDetails?.projectName}. Please try again.`,
                'alert',
              );
            }

            setCodeSpaceData({
              ...res,
              running: !!res.intiatedOn,
            });
          } catch (err: any) {
            console.log(err);
          }
        })
        .catch((err: Error) => {
          clearInterval(livelinessInterval);
          setIsApiCallTakeTime(false);
          ProgressIndicator.hide();
          Notification.show('Error in validating code space deployment - ' + err.message, 'alert');
        });
    }, 2000);
    setLivelinessInterval(intervalId);
  };

  const goBack = () => {
    clearInterval(livelinessInterval);
    Tooltip.clear();
    history.goBack();
  };

  const handleOpenDoraMetrics = () => {
    console.log('ToDo - Dora metrics');
    // setShowDoraMetricsModal(true);
  };

  const projectDetails = codeSpaceData?.projectDetails;
  const disableDeployment =
    projectDetails?.recipeDetails?.recipeId.startsWith('public') ||
    DEPLOYMENT_DISABLED_RECIPE_IDS.includes(projectDetails?.recipeDetails?.recipeId);
  const deployingInProgress =
    projectDetails?.intDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED' ||
    projectDetails?.prodDeploymentDetails?.lastDeploymentStatus === 'DEPLOY_REQUESTED';
  const securedWithIAMContent: React.ReactNode = (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      stroke="#00adef"
      fill="#00adef"
      strokeWidth="0"
      viewBox="0 0 30 30"
      width="15px"
      height="15px"
    >
      {' '}
      <path d="M 15 2 C 11.145666 2 8 5.1456661 8 9 L 8 11 L 6 11 C 4.895 11 4 11.895 4 13 L 4 25 C 4 26.105 4.895 27 6 27 L 24 27 C 25.105 27 26 26.105 26 25 L 26 13 C 26 11.895 25.105 11 24 11 L 22 11 L 22 9 C 22 5.2715823 19.036581 2.2685653 15.355469 2.0722656 A 1.0001 1.0001 0 0 0 15 2 z M 15 4 C 17.773666 4 20 6.2263339 20 9 L 20 11 L 10 11 L 10 9 C 10 6.2263339 12.226334 4 15 4 z" />
    </svg>
  );

  const collaborator = projectDetails?.projectCollaborators?.find((collaborator) => {return collaborator?.id === props?.user?.id })

  const isOwner = projectDetails?.projectOwner?.id === props.user.id || collaborator?.isAdmin;

  const navigateSecurityConfig = () => {
    if (projectDetails?.publishedSecuirtyConfig) {
      window.open(
        `${window.location.pathname}#/codespace/publishedSecurityconfig/${codeSpaceData.id}?name=${projectDetails.projectName}`,
        '_blank',
      );
      return;
    }
    window.open(
      `${window.location.pathname}#/codespace/securityconfig/${codeSpaceData.id}?name=${projectDetails.projectName}`,
      '_blank',
    );
  };

  const intDeploymentDetails = projectDetails?.intDeploymentDetails;
  const prodDeploymentDetails = projectDetails?.prodDeploymentDetails;

  return (
    <div
      id="codespace-view"
      className={fullScreenMode ? Styles.codeSpaceWrapperFSmode : '' + ' ' + Styles.codeSpaceWrapper}
    >
      {codeSpaceData.running && (
        <React.Fragment>
          <div className={Styles.nbheader}>
            <div className={Styles.headerdetails}>
              <img src={Envs.DNA_BRAND_LOGO_URL} className={Styles.Logo} />
              <div className={Styles.nbtitle}>
                <button tooltip-data="Go Back" className="btn btn-text back arrow" onClick={goBack}></button>
                <h2 tooltip-data={recipes.find((item: any) => item.id === projectDetails.recipeDetails.recipeId).name}>
                  {projectDetails.projectName}
                </h2>
              </div>
            </div>
            <div className={Styles.navigation}>
              {codeSpaceData.running && (
                <div className={Styles.headerright}>
                  {!disableDeployment && (
                    <>
                      {(isOwner && !deployingInProgress) && (
                        <div
                          className={classNames(Styles.configLink, Styles.pointer)}
                          onClick={() => navigateSecurityConfig()}
                        >
                          <a target="_blank" rel="noreferrer">
                            <IconGear size={'16'} /> {CODE_SPACE_TITLE} (
                            {projectDetails?.publishedSecuirtyConfig?.status ||
                              projectDetails?.securityConfig?.status ||
                              'New'}
                            )
                          </a>
                          &nbsp;
                        </div>
                      )}
                      {/* {codeDeployed && (
                        <div className={Styles.urlLink} tooltip-data="APP BASE URL - Staging">
                          <a href={codeDeployedUrl} target="_blank" rel="noreferrer">
                            <i className="icon mbc-icon link" /> Staging (
                            <i className="icon mbc-icon transactionaldata" /> {codeDeployedBranch})
                            {intDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
                          </a>
                          &nbsp;
                          <a target="_blank" href={buildLogViewURL(codeDeployedUrl, true)} rel="noreferrer">
                            <i
                              tooltip-data="Show Staging App logs in new tab"
                              className="icon mbc-icon workspace small right"
                            />
                          </a>
                          <div>
                            (
                            {intDeploymentDetails?.gitjobRunID ? (
                              <a
                                target="_blank"
                                href={buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
                                tooltip-data="Show staging build & deploy logs in new tab"
                                rel="noreferrer"
                              >
                                {regionalDateAndTimeConversionSolution(intDeploymentDetails?.lastDeployedOn)}
                              </a>
                            ) : (
                              <>{regionalDateAndTimeConversionSolution(intDeploymentDetails?.lastDeployedOn)}</>
                            )}
                            )
                          </div>
                        </div>
                      )}
                      {intCodeDeployFailed && (
                        <div tooltip-data="Last deployement failed on Staging - Click to view logs">
                          <a
                            target="_blank"
                            className={classNames(Styles.error)}
                            href={buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
                            rel="noreferrer"
                          >
                            <i className="icon mbc-icon alert circle small right" />
                          </a>
                        </div>
                      )}
                      {prodCodeDeployed && (
                        <div className={Styles.urlLink} tooltip-data="APP BASE URL - Production">
                          <a href={prodCodeDeployedUrl} target="_blank" rel="noreferrer">
                            <i className="icon mbc-icon link" /> Production (
                            <i className="icon mbc-icon transactionaldata" /> {prodCodeDeployedBranch})
                            {prodDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
                          </a>
                          &nbsp;
                          <a target="_blank" href={buildLogViewURL(prodCodeDeployedUrl)} rel="noreferrer">
                            <i
                              tooltip-data="Show Production App logs in new tab"
                              className="icon mbc-icon workspace small right"
                            />
                          </a>
                          <div>
                            (
                            {prodDeploymentDetails?.gitjobRunID ? (
                              <a
                                target="_blank"
                                href={buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
                                tooltip-data="Show production build & deploy logs in new tab"
                                rel="noreferrer"
                              >
                                {regionalDateAndTimeConversionSolution(prodDeploymentDetails?.lastDeployedOn)}
                              </a>
                            ) : (
                              <>{regionalDateAndTimeConversionSolution(prodDeploymentDetails?.lastDeployedOn)}</>
                            )}
                            )
                          </div>
                        </div>
                      )}
                      {prodCodeDeployFailed && (
                        <div tooltip-data="Last deployement failed on Production - Click to view logs">
                          <a
                            target="_blank"
                            className={classNames(Styles.error)}
                            href={buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
                            rel="noreferrer"
                          >
                            <i className="icon mbc-icon alert circle small right" />
                          </a>
                        </div>
                      )} */}
                      <div>
                        <button
                          className={classNames('btn btn-secondary', codeDeploying ? 'disable' : '')}
                          onClick={onShowCodeDeployModal}
                        >
                          Deploy{codeDeploying && 'ing...'}
                        </button>
                      </div>
                      {(intDeploymentDetails.lastDeploymentStatus || prodDeploymentDetails.lastDeploymentStatus) && (
                        <div
                          tooltip-data="Show/Hide App Logs Panel"
                          className={classNames(Styles.showLogs, showLogsView && Styles.active)}
                          onClick={toggleLogView}
                        >
                          <i className="icon mbc-icon workspace small right"></i>
                        </div>
                      )}
                    </>
                  )}
                  <div tooltip-data="Open in new tab" className={Styles.OpenNewTab} onClick={openInNewtab}>
                    <i className="icon mbc-icon arrow small right" />
                    <span> &nbsp; </span>
                  </div>
                  <div onClick={toggleFullScreenMode}>
                    <FullScreenModeIcon fsNeed={fullScreenMode} />
                  </div>
                  {!disableDeployment && <div>
                    <span
                      onClick={toggleContextMenu}
                      className={classNames(Styles.trigger, showContextMenu ? Styles.open : '')}
                    >
                      <i className="icon mbc-icon listItem context" />
                    </span>
                    <div
                      style={{
                        top: contextMenuOffsetTop + 'px',
                        left: contextMenuOffsetLeft + 'px',
                        zIndex: 5,
                      }}
                      className={classNames('contextMenuWrapper', Styles.contextMenu, showContextMenu ? '' : 'hide')}
                    >
                      <ul>
                        {projectDetails?.gitRepoName && (
                          <>
                            <li>
                              <a target="_blank" href={buildGitUrl(projectDetails?.gitRepoName)} rel="noreferrer">
                                Go to code repo
                                <i className="icon mbc-icon new-tab" />
                              </a>
                            </li>
                            <li>
                              <hr />
                            </li>
                          </>
                        )}
                        <li>
                          <strong>Staging:</strong>{' '}
                          {intDeploymentDetails?.lastDeployedBranch
                            ? `[Branch - ${codeDeployedBranch}]`
                            : 'No Deployment'}
                          <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                            (DORA Metrics)
                          </span>
                        </li>
                        {isAPIRecipe && (
                          <li>
                            <span
                              onClick={() => {
                                setShowVaultManagementModal(true);
                                setIsStaging(true);
                              }}
                            >
                              Environment variables config
                            </span>
                          </li>
                        )}
                        {intDeploymentDetails?.gitjobRunID && (
                          <li>
                            <a
                              target="_blank"
                              href={buildGitJobLogViewURL(intDeploymentDetails?.gitjobRunID)}
                              rel="noreferrer"
                            >
                              Last Build &amp; Deploy Logs{' '}
                              {intCodeDeployFailed && <span className={classNames(Styles.error)}>[Failed]</span>}{' '}
                              <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {codeDeployed && (
                          <li>
                            <a href={codeDeployedUrl} target="_blank" rel="noreferrer">
                              Deployed App URL {intDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
                              <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {intDeploymentDetails?.lastDeploymentStatus && (
                          <li>
                            <a
                              target="_blank"
                              href={buildLogViewURL(codeDeployedUrl || projectDetails?.projectName.toLowerCase(), true)}
                              rel="noreferrer"
                            >
                              Application Logs <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {intDeploymentDetails?.deploymentAuditLogs && (
                          <li>
                            <span
                              onClick={() => {
                                setShowAuditLogsModal(true);
                                setIsStaging(true);
                                setlogsList(intDeploymentDetails?.deploymentAuditLogs);
                              }}
                            >
                              Deployment Audit Logs
                            </span>
                          </li>
                        )}
                        <li>
                          <hr />
                        </li>
                        <li>
                          <strong>Production:</strong>{' '}
                          {prodDeploymentDetails?.lastDeployedBranch
                            ? `[Branch - ${prodCodeDeployedBranch}]`
                            : 'No Deployment'}
                          <span className={classNames(Styles.metricsTrigger, 'hide')} onClick={handleOpenDoraMetrics}>
                            (DORA Metrics)
                          </span>
                        </li>
                        {isAPIRecipe && (
                          <li>
                            <span
                              onClick={() => {
                                setShowVaultManagementModal(true);
                                setIsStaging(false);
                              }}
                            >
                              Environment variables config
                            </span>
                          </li>
                        )}
                        {prodDeploymentDetails?.gitjobRunID && (
                          <li>
                            <a
                              target="_blank"
                              href={buildGitJobLogViewURL(prodDeploymentDetails?.gitjobRunID)}
                              rel="noreferrer"
                            >
                              Build &amp; Deploy Logs{' '}
                              {prodCodeDeployFailed && <span className={classNames(Styles.error)}>[Failed]</span>}{' '}
                              <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {prodCodeDeployed && (
                          <li>
                            <a href={prodCodeDeployedUrl} target="_blank" rel="noreferrer">
                              Deployed App URL {prodDeploymentDetails?.secureWithIAMRequired && securedWithIAMContent}
                              <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {prodDeploymentDetails?.lastDeploymentStatus && (
                          <li>
                            <a
                              target="_blank"
                              href={buildLogViewURL(prodCodeDeployedUrl || projectDetails?.projectName.toLowerCase())}
                              rel="noreferrer"
                            >
                              Application Logs <i className="icon mbc-icon new-tab" />
                            </a>
                          </li>
                        )}
                        {prodDeploymentDetails?.deploymentAuditLogs && (
                          <li>
                            <span
                              onClick={() => {
                                setShowAuditLogsModal(true);
                                setIsStaging(false);
                                setlogsList(prodDeploymentDetails?.deploymentAuditLogs);
                              }}
                            >
                              Deployment Audit Logs
                            </span>
                          </li>
                        )}
                      </ul>
                    </div>
                  </div>}
                </div>
              )}
            </div>
          </div>
          <div className={Styles.codeSpaceContent}>
            <div className={Styles.codeSpace}>
              {loading ? (
                <div className={'progress-block-wrapper ' + Styles.preloaderCutomnize}>
                  <div className="progress infinite" />
                </div>
              ) : (
                codeSpaceData.running && (
                  <div className={Styles.codespaceframe}>
                    <iframe
                      className={fullScreenMode ? Styles.fullscreen : ''}
                      src={codeSpaceData.workspaceUrl}
                      title="Code Space"
                      allow="clipboard-read; clipboard-write"
                    />
                    {(intDeploymentDetails.lastDeploymentStatus || prodDeploymentDetails.lastDeploymentStatus) &&
                      showLogsView && (
                        <div className={classNames(Styles.logViewWrapper, showLogsView && Styles.show)}>
                          <button
                            className={classNames('link-btn', Styles.closeButton)}
                            onClick={() => setShowLogsView(false)}
                          >
                            <i className="icon mbc-icon close thin"></i>
                          </button>
                          <div className={classNames('tabs-panel', Styles.tabsHeightFix)}>
                            <div className="tabs-wrapper">
                              <ul className="tabs">
                                {intDeploymentDetails.lastDeploymentStatus && (
                                  <li className={'tab active'}>
                                    <a href="#tab-staginglogpanel" id="staginglogpanel">
                                      Staging App Logs
                                    </a>
                                  </li>
                                )}
                                {prodDeploymentDetails.lastDeploymentStatus && (
                                  <li className={classNames('tab', !codeDeployed && 'active')}>
                                    <a href="#tab-productionlogpanel" id="productionlogpanel">
                                      Production App Logs
                                    </a>
                                  </li>
                                )}
                              </ul>
                            </div>
                            <div className={classNames(Styles.logsTabContentWrapper, 'tabs-content-wrapper')}>
                              {intDeploymentDetails?.lastDeploymentStatus && (
                                <div
                                  id="tab-staginglogpanel"
                                  className={classNames(Styles.tabsHeightFix, 'tab-content')}
                                >
                                  <iframe
                                    src={buildLogViewURL(
                                      codeDeployedUrl || projectDetails?.projectName.toLowerCase(),
                                      true,
                                    )}
                                    height="100%"
                                    width="100%"
                                  />
                                </div>
                              )}
                              {prodDeploymentDetails?.lastDeploymentStatus && (
                                <div
                                  id="tab-productionlogpanel"
                                  className={classNames(Styles.tabsHeightFix, 'tab-content')}
                                >
                                  <iframe
                                    src={buildLogViewURL(
                                      prodCodeDeployedUrl || projectDetails?.projectName.toLowerCase(),
                                    )}
                                    height="100%"
                                    width="100%"
                                  />
                                </div>
                              )}
                            </div>
                          </div>
                        </div>
                      )}
                    <div className={Styles.textRight}>
                      <small>
                        Made with{' '}
                        <svg
                          stroke="#e84d47"
                          fill="#e84d47"
                          strokeWidth="0"
                          viewBox="0 0 512 512"
                          height="1em"
                          width="1em"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path d="M462.3 62.6C407.5 15.9 326 24.3 275.7 76.2L256 96.5l-19.7-20.3C186.1 24.3 104.5 15.9 49.7 62.6c-62.8 53.6-66.1 149.8-9.9 207.9l193.5 199.8c12.5 12.9 32.8 12.9 45.3 0l193.5-199.8c56.3-58.1 53-154.3-9.8-207.9z"></path>
                        </svg>{' '}
                        by Developers for Developers
                      </small>
                    </div>
                  </div>
                )
              )}
            </div>
          </div>
        </React.Fragment>
      )}
      {!codeSpaceData.running && showNewCodeSpaceModal && (
        <Modal
          title={''}
          hiddenTitle={true}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth="800px"
          buttonAlignment="right"
          show={showNewCodeSpaceModal}
          content={
            <NewCodeSpace
              user={props.user}
              isCodeSpaceCreationSuccess={isCodeSpaceCreationSuccess}
              toggleProgressMessage={toggleProgressMessage}
            />
          }
          scrollableContent={true}
          onCancel={onNewCodeSpaceModalCancel}
        />
      )}
      {showVaultManagementModal && (
        <Modal
          title={isStaging ? 'Secret Management - Staging' : 'Secret Management - Production'}
          hiddenTitle={false}
          showAcceptButton={false}
          showCancelButton={false}
          acceptButtonTitle="Save"
          onAccept={() => console.log('save')}
          modalWidth={'70%'}
          modalStyle={{ minHeight: '86%' }}
          buttonAlignment="center"
          show={showVaultManagementModal}
          content={<VaultManagement projectName={projectDetails.projectName.toLowerCase()} isStaging={isStaging} />}
          scrollableContent={true}
          onCancel={() => setShowVaultManagementModal(false)}
        />
      )}
      {showAuditLogsModal && (
        <DeployAuditLogsModal
          deployedEnvInfo={isStaging ? 'Staging' : 'Production'}
          show={showAuditLogsModal}
          setShowAuditLogsModal={setShowAuditLogsModal}
          logsList={logsList}
        />
      )}

      {showCodeDeployModal && (
        <DeployModal
          userInfo={props.user}
          codeSpaceData={codeSpaceData}
          enableSecureWithIAM={isIAMRecipe}
          setShowCodeDeployModal={setShowCodeDeployModal}
          startDeployLivelinessCheck={enableDeployLivelinessCheck}
          setCodeDeploying={setCodeDeploying}
          setIsApiCallTakeTime={setIsApiCallTakeTime}
          navigateSecurityConfig={navigateSecurityConfig}
        />
      )}

      {isApiCallTakeTime && (
        <ProgressWithMessage message={'Please wait as this process can take up 2 to 5 minutes....'} />
      )}

      {!serverStarted && (
        <ProgressWithMessage message={`Starting...${serverProgress}%`} />
      )}
    </div>
  );
};

export default CodeSpace;
