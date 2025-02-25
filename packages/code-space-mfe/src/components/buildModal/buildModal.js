import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import Styles from './buildModal.scss';
// @ts-ignore
import Notification from '../../common/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

import { CodeSpaceApiClient } from '../../apis/codespace.api';
import SelectBox from 'dna-container/SelectBox';
import Modal from 'dna-container/Modal';
import { SESSION_STORAGE_KEYS } from '../../Utility/constants.js';
import { regionalDateAndTimeConversionSolution } from '../../Utility/utils';
import TextBox from 'dna-container/TextBox';
import Tags from 'dna-container/Tags';
import Tooltip from '../../common/modules/uilab/js/src/tooltip';
import Pagination from 'dna-container/Pagination';
import DeployModal from '../deployModal/DeployModal';

const BuildModal = (props) => {

  const [branches, setBranches] = useState([]);
  const [branchValue, setBranchValue] = useState(['main']);
  const [isBranchValueMissing, setIsBranchValueMissing] = useState(false);
  // const [buildType, setBuildType] = useState('build');
  const [comment, setComment] = useState('');
  const [totalNumberOfRecords, setTotalNumberOfRecords] = useState();
  const [totalNumberOfPages, setTotalNumberOfPages] = useState(1);
  const [currentPageNumber, setCurrentPageNumber] = useState(1);
  const [paginatedRecords, setPaginatedRecords] = useState([]);
  const [maxItemsPerPage, setMaxItemsPerPage] = useState(
    parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.AUDIT_LOGS_MAX_ITEMS_PER_PAGE), 10) || 5,
  );
  const [allLogs, setAllLogs] = useState([]);
  const [showDeployCodeSpaceModal, setShowDeployCodeSpaceModal] = useState(false);
  const [buildDetails, setBuildDetails] = useState('');

  const projectDetails = props.codeSpaceData?.projectDetails;

  useEffect(() => {
    Tooltip.defaultSetup();
    ProgressIndicator.show();
    setAllLogs([]);
    CodeSpaceApiClient.getCodeSpacesGitBranchList(projectDetails?.gitRepoName)
      .then((res) => {
        ProgressIndicator.hide();
        props.setShowCodeBuildModal(true);
        let branches = res?.data;
        branches.forEach((element) => {
          element.id = element.name;
        });
        setBranches(branches);

        SelectBox.defaultSetup();
      })
      .catch((err) => {
        ProgressIndicator.hide();
        Notification.show('Error in getting code space branch list - ' + err.message, 'alert');
      });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    setTotalNumberOfRecords(allLogs.length);
    setTotalNumberOfPages(Math.ceil(allLogs.length / maxItemsPerPage));
    setPaginatedRecords(allLogs.slice(0, 0 + maxItemsPerPage));
    setCurrentPageNumber(1);
  }, [allLogs]);

  useEffect(() => {
    Tooltip.defaultSetup();
  }, [paginatedRecords]);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = allLogs.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    setPaginatedRecords(modifiedData);
  };

  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumberTemp + 1;
    const modifiedData = allLogs.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    setCurrentPageNumber(currentPageNumberTemp);
    setPaginatedRecords(modifiedData);
  };

  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(allLogs?.length / pageNum);
    const modifiedData = allLogs.slice(0, pageNum);
    setPaginatedRecords(modifiedData);
    setTotalNumberOfPages(totalNumberOfPages);
    setMaxItemsPerPage(pageNum);
    setCurrentPageNumber(1);
  };

  const onCommentChange = (evnt) => {
    setComment(evnt.currentTarget.value);
  };

  const onBranchChange = (selectedTags) => {
    setBranchValue(selectedTags);
    setIsBranchValueMissing(false);
  };

  const onBuildTrigger = () => {
    let formValid = true;
    if (!branchValue.length) {
      formValid = false;
      setIsBranchValueMissing(true);
    }
    formValid && console.log('buid triggered');
  };

  return (
    <Modal
      title={'Manage Build'}
      showAcceptButton={false}
      //   acceptButtonTitle={'Deploy'}
      //   cancelButtonTitle={'Cancel'}
      //   onAccept={onAcceptCodeBuild}
      showCancelButton={false}
      modalWidth="1000px"
      buttonAlignment="center"
      show={true}
      content={
        <>
          <div className={Styles.BuildModal}>
            <p>The code from your workspace will be built and you can check the status on the build logs.</p>
            <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
              <div>
                <Tags
                  title={'Select Branch'}
                  max={1}
                  chips={branchValue}
                  placeholder={'Type here...'}
                  tags={branches}
                  setTags={onBranchChange}
                  isMandatory={true}
                  showMissingEntryError={isBranchValueMissing}
                  showAllTagsOnFocus={true}
                  disableSelfTagAdd={true}
                  suggestionPopupHeight={150}
                />
              </div>
              <div>
                <TextBox
                  type="text"
                  controlId={'commentsInput'}
                  labelId={'commentsLabel'}
                  label={'Comments'}
                  placeholder={'Type here'}
                  value={comment}
                  required={false}
                  maxLength={60}
                  onChange={onCommentChange}
                />
              </div>
              <div className={Styles.btnGrp}>
                <button
                  className={'btn btn-primary ' + classNames(Styles.triggerBtn)}
                  type="button"
                  onClick={onBuildTrigger}
                >
                  Trigger Build
                </button>
              </div>
            </div>
            <div className={classNames(Styles.wrapper)}>
              <div className={Styles.flexLayout}>
                <div>
                  <h4>Build Audit</h4>
                </div>
                <div>
                  <button
                    className={classNames('btn btn-primary', Styles.refreshBtn)}
                    tooltip-data="Refresh logs"
                    onClick={() => {
                      console.log('refresh');
                    }}
                  >
                    <i className="icon mbc-icon refresh" />
                  </button>
                </div>
              </div>
              <div className={classNames(Styles.codeSpaceBuildListviewContent)}>
                <table className={classNames('ul-table solutions', Styles.codeSpaceBuildMargininone)}>
                  <thead>
                    <tr className={classNames('header-row')}>
                      <th>
                        <label>Branch</label>
                      </th>
                      <th>
                        <label>Triggered By</label>
                      </th>
                      <th>
                        <label>Triggered On</label>
                      </th>
                      <th>
                        <label>Build Status</label>
                      </th>
                      <th>
                        <label>Artifact ID</label>
                      </th>
                      <th>
                        <label>Version</label>
                      </th>
                      <th>
                        <label>Comments</label>
                      </th>
                      <th>
                        <label>Deploy</label>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedRecords.map((item, index) => (
                      <tr className={classNames('data-row')} key={index}>
                        <td>{item?.branch}</td>
                        <td>{item?.triggeredBy}</td>
                        <td>{regionalDateAndTimeConversionSolution(item?.triggeredOn)}</td>
                        <td>
                          {/* {item?.buildStatus}
                          <button
                            className={classNames('btn btn-primary', Styles.actionBtn)}
                            tooltip-data="Navigate to OpenSearch logs"
                            onClick={() => {
                              console.log('logs');
                            }}
                          >
                            <i className="icon mbc-icon new-tab" />
                          </button> */}
                          <a
                            target="_blank"
                            href={''}
                            rel="noreferrer"
                            className={classNames(Styles.newLink)}
                            style={{ color: item?.buildStatus === 'SUCCESS' ? '#12e7ab' : item?.buildStatus === 'BUILD_FAILED' ? '#e94d47' : '#f3e537' }}
                          >
                            {item?.buildStatus === 'SUCCESS' ? 'Success' : item?.buildStatus === 'BUILD_FAILED' ? 'Failed' : 'In Progress'}
                            <i className="icon mbc-icon new-tab small" />
                          </a>
                        </td>
                        <td>{item?.artifactId || 'N/A'}</td>
                        <td>{item?.version || 'N/A'}</td>
                        <td><label>{item?.comment || 'N/A'}</label></td>
                        <td>
                          {item?.buildStatus === 'SUCCESS' ? (
                            <button
                              className={'btn btn-primary ' + classNames(Styles.actionBtn)}
                              tooltip-data="Deploy application"
                              onClick={() => {
                                console.log('deploy');
                                setBuildDetails(item);
                                setShowDeployCodeSpaceModal(true);
                              }}
                            >
                              <i className="icon mbc-icon deploy" />
                            </button>
                          ) : (
                            ''
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
          {totalNumberOfRecords ? (
            <Pagination
              totalPages={totalNumberOfPages}
              pageNumber={currentPageNumber}
              onPreviousClick={onPaginationPreviousClick}
              onNextClick={onPaginationNextClick}
              onViewByNumbers={onViewByPageNum}
              displayByPage={true}
              startWithFive={true}
            />
          ) : null}
          {showDeployCodeSpaceModal && (
            <DeployModal
            userInfo={props.userInfo}
            codeSpaceData={props.codeSpaceData}
            // enableSecureWithIAM={props.enableSecureWithIAM}
            // isUIRecipe={props.isUIRecipe}
            setShowCodeDeployModal={setShowDeployCodeSpaceModal}
            setCodeDeploying={props.setCodeDeploying}
            setIsApiCallTakeTime={props.setIsApiCallTakeTime}
            navigateSecurityConfig={props.navigateSecurityConfig}
            buildDetails={buildDetails}
            />
          )}
        </>
      }
      scrollableContent={false}
      scrollableBox={true}
      onCancel={() => props.setShowCodeBuildModal(false)}
    />
  );
};
export default BuildModal;
