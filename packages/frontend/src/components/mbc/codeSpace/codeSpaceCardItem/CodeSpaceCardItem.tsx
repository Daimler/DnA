import classNames from 'classnames';
import React, { useState } from 'react';
import Styles from './CodeSpaceCardItem.scss';
import { recipesMaster, regionalDateAndTimeConversionSolution } from '../../../../services/utils';
import ConfirmModal from 'components/formElements/modal/confirmModal/ConfirmModal';
import { history } from '../../../../router/History';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { ICodeSpaceData } from '../CodeSpace';
import { CodeSpaceApiClient } from '../../../../services/CodeSpaceApiClient';
import { trackEvent } from '../../../../services/utils';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
import { IUserInfo } from 'globals/types';
import { IconGear } from 'components/icons/IconGear';

interface CodeSpaceCardItemProps {
  userInfo: IUserInfo;
  codeSpace: ICodeSpaceData;
  onDeleteSuccess?: () => void;
  toggleProgressMessage?: (show: boolean) => void;
  onShowCodeSpaceOnBoard: (codeSpace: ICodeSpaceData) => void;
  onCodeSpaceEdit: (codeSpace: ICodeSpaceData) => void;
}

const CodeSpaceCardItem = (props: CodeSpaceCardItemProps) => {
  const codeSpace = props.codeSpace;
  // const collaborationCodeSpace = codeSpace.projectDetails.projectCollaborators?.find((user: ICodeCollaborator) => user.id === props.userInfo.id);
  const enableOnboard = codeSpace ? codeSpace.status === 'COLLABORATION_REQUESTED' : false;
  // const codeDeploying = codeSpace.status === 'DEPLOY_REQUESTED';
  const deleteInProgress = codeSpace.status === 'DELETE_REQUESTED';
  const createInProgress = codeSpace.status === 'CREATE_REQUESTED';
  const creationFailed = codeSpace.status === 'CREATE_FAILED';
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const recipes = recipesMaster;
  const isOwner = codeSpace.projectDetails?.projectOwner?.id === props.userInfo.id;
  const hasCollaborators = codeSpace.projectDetails?.projectCollaborators?.length > 0;

  const deleteCodeSpaceContent = (
    <div>
      <h3>
        {/* Are you sure to delete {codeSpace.projectDetails.projectName} Code Space?
        <br /> */}
        {isOwner ? (
          <>
            {hasCollaborators ? (
              <>
                You have collaborators in your project.
                <br />
                Please transfer your ownership to any one of the collaborator <br /> or remove the collaborator(s) before
                deleting this code space '{codeSpace?.projectDetails?.projectName}'.
              </>
            ) : (
              <>
                Deleting a CodeSpace would delete the code associated with it,
                <br /> Do you want to proceed?
              </>
            )}
          </>
        ) : (
          <>
            You were asked to collaborate on this CodeSpace by your colleague.
            <br />
            Deleting this CodeSpace will revoke your access to collaborate.
            <br />
            Do you wish to proceed?
          </>
        )}
      </h3>
    </div>
  );

  const deleteCodeSpaceAccept = () => {
    ProgressIndicator.show();
    CodeSpaceApiClient.deleteCodeSpace(codeSpace.id)
      .then((res: any) => {
        trackEvent('DnA Code Space', 'Delete', 'Delete code space');
        if (res.success === 'SUCCESS') {
          props.onDeleteSuccess();
          setShowDeleteModal(false);
          ProgressIndicator.hide();
          Notification.show(`Code space '${codeSpace.projectDetails?.projectName}' has been deleted successfully.`);
        } else {
          ProgressIndicator.hide();
          Notification.show(
            'Error in deleting code space. Please try again later.\n' + res.errors[0].message,
            'alert',
          );
        }
      })
      .catch((err: Error) => {
        ProgressIndicator.hide();
        Notification.show('Error in deleting code space. Please try again later.\n' + err.message, 'alert');
      });
  };
  const deleteCodeSpaceClose = () => {
    setShowDeleteModal(false);
  };

  const onCardNameClick = () => {
    if (enableOnboard) {
      props.onShowCodeSpaceOnBoard(codeSpace);
    } else {
      history.push(`codespace/${codeSpace.workspaceId}`);
    }
  };

  const onCodeSpaceSecurityConfigClick = (codeSpace: ICodeSpaceData) => {
      history.push(`codespace/securityconfig/${codeSpace.id}`);
  };

  const onCodeSpaceDelete = () => {
    if (creationFailed) {
      deleteCodeSpaceAccept();
    } else {
      setShowDeleteModal(true);
    }
  };

  const projectDetails = codeSpace?.projectDetails;
  const intDeploymentDetails = projectDetails.intDeploymentDetails;
  const prodDeploymentDetails = projectDetails.prodDeploymentDetails;
  const intDeployedUrl = intDeploymentDetails?.deploymentUrl;
  const intLastDeployedOn = intDeploymentDetails?.lastDeployedOn;
  const prodDeployedUrl = prodDeploymentDetails?.deploymentUrl;
  const prodLastDeployedOn = prodDeploymentDetails?.lastDeployedOn;
  const deployingInProgress =
    intDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED' ||
    prodDeploymentDetails.lastDeploymentStatus === 'DEPLOY_REQUESTED';
  const intDeployed =
    intDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
    (intDeployedUrl !== null && intDeployedUrl !== 'null');
  const prodDeployed =
    prodDeploymentDetails.lastDeploymentStatus === 'DEPLOYED' ||
    (prodDeployedUrl !== null && prodDeployedUrl !== 'null');

  const deployed = intDeployed || prodDeployed;
  const allowDelete = isOwner ? !hasCollaborators : true;
  const isPublicRecipe = projectDetails.recipeDetails?.recipeId.startsWith('public');

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

  return (
    <>
      <div className={classNames(Styles.codeSpaceCard, deleteInProgress || createInProgress ? Styles.disable : null)}>
        <div className={Styles.cardHead}>
          <div
            className={classNames(
              Styles.cardHeadInfo,
              deleteInProgress || createInProgress || creationFailed ? Styles.disable : null,
            )}
          >
            <div className={classNames('btn btn-text forward arrow', Styles.cardHeadTitle)} onClick={onCardNameClick}>
              {projectDetails.projectName}
            </div>
          </div>
        </div>
        <hr />
        <div className={Styles.cardBodySection}>
          <div>
            <div>
              <div>Code Recipe</div>
              <div>{recipes.find((item: any) => item.id === projectDetails.recipeDetails.recipeId)?.name}</div>
            </div>
            <div>
              <div>Environment</div>
              <div>{projectDetails.recipeDetails.cloudServiceProvider}</div>
            </div>
            <div>
              <div>Created on</div>
              <div>{regionalDateAndTimeConversionSolution(codeSpace?.projectDetails.projectCreatedOn)}</div>
            </div>
            {deployed && (
              <div>
                <div>Last Deployed on</div>
                <div>
                  {intDeployed && (
                    <>
                      Staging({intDeploymentDetails.lastDeployedBranch}):
                      <br />
                      {regionalDateAndTimeConversionSolution(intLastDeployedOn)}
                    </>
                  )}
                  <br />
                  {prodDeployed && (
                    <>
                      Production({prodDeploymentDetails.lastDeployedBranch}):
                      <br />
                      {regionalDateAndTimeConversionSolution(prodLastDeployedOn)}
                    </>
                  )}
                </div>
              </div>
            )}
            {/* <div>
              <div>Code Space ID</div>
              <div>{codeSpace.name}</div>
            </div> */}
          </div>
        </div>
        <div className={Styles.cardFooter}>
          {enableOnboard ? (
            <div>
              <span className={classNames(Styles.statusIndicator, Styles.colloboration)}>
                Collaboration Requested...
              </span>
            </div>
          ) : (
            <>
              <div>
                {createInProgress ? (
                  <span className={classNames(Styles.statusIndicator, Styles.creating)}>Creating...</span>
                ) : (
                  <>
                    {deployingInProgress && (
                      <span className={classNames(Styles.statusIndicator, Styles.deploying)}>Deploying...</span>
                    )}
                    {deployed && (
                      <>
                        {!deployingInProgress && <span className={Styles.statusIndicator}>Deployed</span>}
                        {intDeployed && (
                          <a href={intDeployedUrl} target="_blank" rel="noreferrer" className={Styles.deployedLink}>
                            <i className="icon mbc-icon link" /> Staging{' '}
                            {projectDetails.intDeploymentDetails.secureWithIAMRequired && (securedWithIAMContent)}
                          </a>
                        )}
                        {prodDeployed && (
                          <a href={prodDeployedUrl} target="_blank" rel="noreferrer" className={Styles.deployedLink}>
                            <i className="icon mbc-icon link" /> Production{' '}
                            {projectDetails.prodDeploymentDetails.secureWithIAMRequired && (securedWithIAMContent)}
                          </a>
                        )}
                      </>
                    )}
                  </>
                )}
                {deleteInProgress && (
                  <span className={classNames(Styles.statusIndicator, Styles.deleting)}>Deleting...</span>
                )}
                {creationFailed && (
                  <span className={classNames(Styles.statusIndicator, Styles.deleting)}>Create Failed</span>
                )}
              </div>
              <div className={Styles.btnGrp}>
              {!isPublicRecipe && !createInProgress && !deployingInProgress && !creationFailed && isOwner && (
                  <button className="btn btn-primary" onClick={() => onCodeSpaceSecurityConfigClick(codeSpace)}>
                    <IconGear size={'18'} />
                  </button>
                )}
                {!isPublicRecipe && !createInProgress && !deployingInProgress && !creationFailed && isOwner && (
                  <button className="btn btn-primary" onClick={() => props.onCodeSpaceEdit(codeSpace)}>
                    <i className="icon mbc-icon edit"></i>
                  </button>
                )}
                {!creationFailed && !deleteInProgress && !createInProgress && !deployingInProgress && (
                  <button className="btn btn-primary" onClick={onCodeSpaceDelete}>
                    <i className="icon delete"></i>
                  </button>
                )}
                {creationFailed && (
                  <button className="btn btn-primary hidden">
                    <i className="icon mbc-icon refresh"></i> Retry
                  </button>
                )}
              </div>
            </>
          )}
        </div>
      </div>
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle={allowDelete ? 'No' : 'OK'}
        showAcceptButton={allowDelete}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteCodeSpaceContent}
        onCancel={deleteCodeSpaceClose}
        onAccept={deleteCodeSpaceAccept}
      />
    </>
  );
};
export default CodeSpaceCardItem;
