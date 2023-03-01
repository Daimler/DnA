import classNames from 'classnames';
import React, { useEffect, useMemo, useState } from 'react';
import Styles from './styles.scss';
import MDEditor from '@uiw/react-md-editor';

import { useDispatch, useSelector } from 'react-redux';

import { Link, withRouter, useParams } from 'react-router-dom';

import { dataProductApi } from '../../../apis/dataproducts.api';
import { hostServer } from '../../../server/api';

import DataTranferCardLayout from '../../dataTransfer/Layout/CardView/DataTransferCardItem';

import { setSelectedDataProduct, setDivisionList, resetDataTransferList } from '../redux/dataProductSlice';

import { isValidURL, regionalDateFormat } from '../../../Utility/utils';

import InfoModal from 'dna-container/InfoModal';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';
import SelectBox from 'dna-container/SelectBox';

import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Tabs from '../../../common/modules/uilab/js/src/tabs';
import Notification from '../../../common/modules/uilab/js/src/notification';
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';

import lockIcon from '../../../assets/lockIcon.png';

import ConsumerForm from '../../dataTransfer/ConsumerForm';
import { deserializeFormData, serializeDivisionSubDivision } from '../../../Utility/formData';
import { SetAllAssociatedDataTransfers, SetMyAssociatedDataTransfers } from '../redux/dataProduct.services';
import { Envs } from '../../../Utility/envs';
import { getCorporateDataCatalogs } from '../../redux/getDropdowns.services';
import { MAP_URLS } from '../../../Utility/constants';

const Summary = ({ history, user }) => {
  const { id: dataProductId } = useParams();
  const {
    selectedDataProduct,
    data: dataList,
    divisionList,
    allDataTransfer,
    myDataTransfer,
  } = useSelector((state) => state.dataProduct);

  const { corporateDataCatalogs } = useSelector((state) => state.dropdowns);

  const dispatch = useDispatch();

  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [currentTab, setCurrentTab] = useState('provider');

  const [showInfoModal, setShowInfoModal] = useState(false);

  const [showRequestAccessModal, setShowRequestAccessModal] = useState(false);
  const [showHowToAccessModal, setShowHowToAccessModal] = useState(false);

  const [showMyDataTransfers, setShowMyDataTransfers] = useState(false);

  const [CDC_URL, setCDCURL] = useState('');

  const isCreator = selectedDataProduct?.createdBy?.id === user?.id;
  const usersAllowedToModify =
    selectedDataProduct?.informationOwner?.shortId === user?.id || selectedDataProduct?.name?.shortId === user?.id;

  const updatedBy = selectedDataProduct?.modifiedBy ? selectedDataProduct.modifiedBy : selectedDataProduct.createdBy;
  const lastModifiedBy = `${updatedBy?.firstName} ${updatedBy?.lastName}`;

  const showContactInformation = selectedDataProduct?.openSegments?.includes('ContactInformation');
  const showConfidentiality = selectedDataProduct?.openSegments?.includes('ClassificationAndConfidentiality');
  const showPersonalData = selectedDataProduct?.openSegments?.includes('IdentifyingPersonalRelatedData');
  const showTransNationalData = selectedDataProduct?.openSegments?.includes('IdentifiyingTransnationalDataTransfer');
  const showDeletionRequirements = selectedDataProduct?.openSegments?.includes('SpecifyDeletionRequirements');

  const division = serializeDivisionSubDivision(divisionList, {
    division: selectedDataProduct.division,
    subDivision: selectedDataProduct.subDivision,
  });

  useEffect(() => {
    dispatch(getCorporateDataCatalogs());
  }, [dispatch]);

  useMemo(() => {
    const CDC_URL = Envs.CORPORATE_DATA_CATALOG_URL;
    const URL = CDC_URL.substring(0, CDC_URL.indexOf('data') + 4);
    const refId = corporateDataCatalogs?.find(
      (item) => item.name === selectedDataProduct?.corporateDataCatalog,
    )?.externalRefId;
    setCDCURL(`${URL}/${refId}`);
  }, [corporateDataCatalogs, selectedDataProduct?.corporateDataCatalog]);

  useEffect(() => {
    ProgressIndicator.show();
    hostServer.get('/divisions').then((res) => {
      dispatch(setDivisionList(res.data));
      ProgressIndicator.hide();
    });
  }, [dispatch]);

  useEffect(() => {
    SelectBox.defaultSetup();
    Tooltip.defaultSetup();
  }, []);

  useEffect(() => {
    getDataProductById();

    return () => {
      dispatch(setSelectedDataProduct({}));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dataList]);

  useEffect(() => {
    return () => {
      dispatch(resetDataTransferList());
    };
  }, [dispatch]);

  const getDataProductById = () => {
    dataProductApi.getDataProductById(dataProductId).then((res) => {
      if (res.status === 204) {
        return history.push('/NotFound');
      } else {
        const data = deserializeFormData({ item: res.data, isDataProduct: true });
        dispatch(setSelectedDataProduct(data));
        Tabs.defaultSetup();
      }
    });
  };

  useEffect(() => {
    const mainPanel = document.getElementById('mainPanel');
    const accessBtnSetDiv = document.querySelector('.accessBtnSet');

    const accessRequestDiv = document.querySelector('.accessRequestInfo');

    const handleScroll = () => {
      if (window.scrollY + window.innerHeight >= mainPanel.scrollHeight) {
        accessBtnSetDiv?.classList.add('fixed');
        accessRequestDiv?.classList.add('fixed');
      } else {
        accessBtnSetDiv?.classList.remove('fixed');
        accessRequestDiv?.classList.remove('fixed');
      }
    };

    window.addEventListener('scroll', handleScroll);

    return () => window.removeEventListener('scroll', handleScroll);

    //eslint-disable-next-line
  }, [isCreator]);

  useEffect(() => {
    if (showInfoModal) {
      // update colors for the markdown editor
      const mdEditor = document.querySelector('.mbc-modal [data-color-mode="dark"]>div.wmde-markdown');

      mdEditor.style.setProperty('--color-canvas-default', 'transparent');
      mdEditor.style.setProperty('font-size', 'inherit');
    }
  }, [showInfoModal]);

  useEffect(() => {
    if (selectedDataProduct?.datatransfersAssociated?.length) {
      dispatch(SetAllAssociatedDataTransfers(selectedDataProduct?.datatransfersAssociated));
      dispatch(SetMyAssociatedDataTransfers(selectedDataProduct?.datatransfersAssociated));
    }
  }, [dispatch, selectedDataProduct?.datatransfersAssociated]);

  useEffect(() => {
    if (myDataTransfer?.totalCount > 0) {
      setShowHowToAccessModal(true);
    }
  }, [myDataTransfer?.totalCount]);

  const setTab = (e) => {
    // e.preventDefault();
    setCurrentTab(e.target.id);
  };

  const isURL = (value) => {
    if (isValidURL(value)) {
      return (
        <a href={value} target="_blank" rel="noopener noreferrer">
          {value}
          <i tooltip-data="Open in New Tab" className={'icon mbc-icon new-tab'} />
        </a>
      );
    } else {
      return value;
    }
  };

  const handleSwitch = () => {
    setShowMyDataTransfers(!showMyDataTransfers);
  };

  const deleteDataProductAccept = () => {
    ProgressIndicator.show();
    dataProductApi.deleteDataProduct(selectedDataProduct?.id).then(() => {
      history.push('/dataproducts');
      setShowDeleteModal(false);
      Notification.show(`${selectedDataProduct?.productName} deleted successfully.`);
    });
  };
  const deleteDataProductClose = () => {
    setShowDeleteModal(false);
  };

  const deleteDataProductContent = (
    <div>
      <h3>Are you sure you want to delete {selectedDataProduct?.productName} ? </h3>
    </div>
  );

  const infoModalHeaderContent = (
    <div className={Styles.titleContainer}>
      <div>
        <img src={lockIcon} style={{ width: '50px', marginRight: 20 }} />
      </div>
      <div>
        <h2>How to access the data</h2>
        <span>Follow the shown steps to gain access and use this product.</span>
      </div>
    </div>
  );

  const infoModalContent = (
    <div data-color-mode="dark">
      <MDEditor.Markdown source={selectedDataProduct.howToAccessText} />
    </div>
  );

  const requestAccessModalContent = (
    <>
      <ConsumerForm isDataProduct={true} callbackFn={() => setShowRequestAccessModal(false)} />
    </>
  );

  const externalLink = (list) => {
    return list.map((item, index) => {
      const lastIndex = list.length - 1 === index;
      return (
        <React.Fragment key={index}>
          <a href={MAP_URLS[item]} target="_blank" rel="noopener noreferrer">
            {item}
            {MAP_URLS[item] ? <i tooltip-data="Open in New Tab" className={'icon mbc-icon new-tab'} /> : null}
          </a>
          &nbsp;{!lastIndex && `\u002F\xa0`}
        </React.Fragment>
      );
    });
  };

  return (
    <div className="dataproductSummary">
      <div id="mainPanel" className={Styles.mainPanel}>
        <div>
          <button className="btn btn-text back arrow" type="submit" onClick={() => history.goBack()}>
            Back
          </button>
          <div className={Styles.actionBtns}>
            <button
              className="btn btn-primary"
              onClick={() =>
                history.push({
                  pathname: '/dataproduct/create',
                  state: { copyId: selectedDataProduct?.dataProductId },
                })
              }
            >
              <i className="icon mbc-icon copy" tooltip-data="Create Copy"></i>Copy & Create New
            </button>
            {isCreator || usersAllowedToModify ? (
              <>
                <button className={classNames('btn btn-primary')} onClick={() => setShowDeleteModal(true)}>
                  <i className="icon mbc-icon delete-new" tooltip-data="Delete"></i>Delete
                </button>
                <button
                  className="btn btn-primary"
                  onClick={() => history.push(`/dataproduct/edit/${selectedDataProduct?.dataProductId}`)}
                >
                  <i className="icon mbc-icon edit fill" tooltip-data="Edit"></i>Edit
                </button>
              </>
            ) : null}
          </div>
          <div className={Styles.summaryBannerTitle}>
            <h2>{selectedDataProduct?.productName}</h2>
          </div>
          <div className={Styles.modifyByLabel}>Last modified by: {lastModifiedBy}</div>
          <div id="data-product-summary-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={currentTab === 'provider' ? 'tab active' : 'tab'}>
                    <a href="#tab-content-1" id="provider" onClick={setTab}>
                      Data Product Summary
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary1" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary2" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary3" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary4" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary5" className={'hidden'}>
                      `
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                <div className={Styles.sectionWrapper}>
                  <div className={Styles.firstPanel}>
                    <div className={Styles.flexLayout}>
                      <div>
                        <h5>Description</h5>
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">Data Product Name</label>
                        <br />
                        {selectedDataProduct.productName}
                      </div>
                      <div>
                        <label className="input-label summary">Agile Release Train</label>
                        <br />
                        {selectedDataProduct?.ART || '-'}
                      </div>
                      <div>
                        <label className="input-label summary">CarLA Function</label>
                        <br />
                        {selectedDataProduct?.carLAFunction || '-'}
                      </div>
                      <div>
                        <label className="input-label summary">Corporate Data Catalog</label>
                        <br />
                        {selectedDataProduct?.corporateDataCatalog ? (
                          <>
                            <a href={CDC_URL} target="_blank" rel="noopener noreferrer">
                              {selectedDataProduct?.corporateDataCatalog}
                              <i tooltip-data="Open in New Tab" className={'icon mbc-icon new-tab'} />
                            </a>{' '}
                          </>
                        ) : (
                          '-'
                        )}
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">Platforms</label>
                        <br />
                        {selectedDataProduct?.platform?.length > 0 ? externalLink(selectedDataProduct?.platform) : '-'}
                      </div>
                      <div>
                        <label className="input-label summary">Front-End Tools</label>
                        <br />
                        {selectedDataProduct?.frontEndTools?.length > 0
                          ? externalLink(selectedDataProduct?.frontEndTools)
                          : '-'}
                      </div>
                      <div>
                        <label className="input-label summary">DDX</label>
                        <br />
                        {isURL(selectedDataProduct?.ddx) || '-'}
                      </div>
                      <div>
                        <label className="input-label summary">Kafka</label>
                        <br />
                        {isURL(selectedDataProduct?.kafka) || '-'}
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">oneAPI</label>
                        <br />
                        {isURL(selectedDataProduct?.oneApi) || '-'}
                      </div>
                    </div>
                    <div className={Styles.flexLayout}>
                      <div>
                        <label className="input-label summary">Data Product Description</label>
                        <br />
                        {selectedDataProduct.description}
                      </div>
                    </div>
                  </div>
                </div>
                {showContactInformation ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Contact Information</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                        <div>
                          <label className="input-label summary">Information Owner</label>
                          <br />
                          {selectedDataProduct.informationOwner?.firstName}{' '}
                          {selectedDataProduct.informationOwner?.lastName}
                        </div>
                        <div>
                          <label className="input-label summary">Publish Date of Data Product</label>
                          <br />
                          {regionalDateFormat(selectedDataProduct.dateOfDataProduct)}
                        </div>
                        <div>
                          <label className="input-label summary">Name</label>
                          <br />
                          {selectedDataProduct?.name?.firstName} {selectedDataProduct?.name?.lastName}
                        </div>
                        <div>
                          <label className="input-label summary">Division</label>
                          <br />
                          {division?.name}
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                        <div>
                          <label className="input-label summary">Sub Division</label>
                          <br />
                          {division?.subdivision?.name || '-'}
                        </div>
                        <div>
                          <label className="input-label summary">Department</label>
                          <br />
                          {selectedDataProduct.department}
                        </div>
                        <div>
                          <label className="input-label summary">PlanningIT App-ID</label>
                          <br />
                          {selectedDataProduct.planningIT || '-'}
                        </div>
                        <div>
                          <label className="input-label summary">Compliance Officer / Responsible (LCO/LCR) </label>
                          <br />
                          {selectedDataProduct.complianceOfficer}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
                {showConfidentiality ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Data Description & Classification</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">Description of transfered data</label>
                          <br />
                          {selectedDataProduct.classificationOfTransferedData}
                        </div>
                        <div>
                          <label className="input-label summary">Confidentiality</label>
                          <br />
                          {selectedDataProduct?.confidentiality}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
                {showPersonalData ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Personal Related Data</h5>
                        </div>
                      </div>
                      <div className={Styles.flexLayout}>
                        <div>
                          <label className="input-label summary">Is data personal related</label>
                          <br />
                          {selectedDataProduct.personalRelatedData}
                        </div>
                      </div>
                      {selectedDataProduct.personalRelatedData === 'Yes' ? (
                        <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                          <div>
                            <label className="input-label summary">Description</label>
                            <br />
                            {selectedDataProduct.personalRelatedDataDescription}
                          </div>
                          <div>
                            <label className="input-label summary">
                              Original (business) purpose of processing this personal related data
                            </label>
                            <br />
                            {selectedDataProduct.personalRelatedDataPurpose}
                          </div>
                          <div>
                            <label className="input-label summary">
                              Original legal basis for processing this personal related data
                            </label>
                            <br />
                            {selectedDataProduct.personalRelatedDataLegalBasis}
                          </div>
                          <div></div>
                        </div>
                      ) : null}
                    </div>
                  </div>
                ) : null}
                {showTransNationalData ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Trans-national Data</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">
                            Is data being transferred from one country to another?
                          </label>
                          <br />
                          {selectedDataProduct.transnationalDataTransfer}
                        </div>
                        {selectedDataProduct.transnationalDataTransfer == 'Yes' ? (
                          <div>
                            <label className="input-label summary">Is one of these countries not within the EU?</label>
                            <br />
                            {selectedDataProduct.transnationalDataTransferNotWithinEU || 'No'}
                          </div>
                        ) : null}
                        {selectedDataProduct.transnationalDataTransfer == 'Yes' &&
                        selectedDataProduct.transnationalDataTransferNotWithinEU == 'Yes' ? (
                          <div>
                            <label className="input-label summary">Has LCO/LCR approved this data transfer?</label>
                            <br />
                            {selectedDataProduct.LCOApprovedDataTransfer}
                          </div>
                        ) : null}
                        <div>
                          <label className="input-label summary">Does product contain insider information?</label>
                          <br />
                          {selectedDataProduct.insiderInformation}
                        </div>
                      </div>
                      <div className={Styles.flexLayout}>
                        <div>
                          <label className="input-label summary">Is data from China included?</label>
                          <br />
                          {selectedDataProduct.dataOriginatedFromChina}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
                {showDeletionRequirements ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Deletion Requirements & Other</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">
                            Are there specific deletion requirements for this data?
                          </label>
                          <br />
                          {selectedDataProduct.deletionRequirement}
                        </div>
                        {selectedDataProduct.deletionRequirement === 'Yes' ? (
                          <div>
                            <label className="input-label summary">Describe deletion requirements</label>
                            <br />
                            {selectedDataProduct.deletionRequirementDescription}
                          </div>
                        ) : null}
                        <div></div>
                      </div>
                      <div className={Styles.flexLayout}>
                        <div>
                          <label className="input-label summary">Other relevant information </label>
                          <br />
                          {selectedDataProduct.otherRelevantInfo || '-'}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
              </div>
            </div>
          </div>
          <hr className={Styles.line} />
          <div className={Styles.dataTransferSection}>
            <h3>{`Data Transfer ${allDataTransfer?.totalCount ? `( ${allDataTransfer?.totalCount} )` : '( 0 )'}`}</h3>
            {allDataTransfer?.totalCount > 0 ? (
              <div>
                <Link to={'/datasharing'} target="_blank" rel="noreferrer noopener">
                  Show in Data Sharing
                  <i tooltip-data="Open in New Tab" className={'icon mbc-icon new-tab'} />
                </Link>
              </div>
            ) : null}
          </div>
          {!isCreator ? (
            <div className={Styles.dataTransferSection}>
              <label className="switch">
                <span className="label" style={{ marginRight: '5px' }}>
                  Show My Data Transfers
                </span>
                <span className="wrapper">
                  <input
                    value={showMyDataTransfers}
                    type="checkbox"
                    className="ff-only"
                    onChange={handleSwitch}
                    checked={showMyDataTransfers}
                  />
                </span>
              </label>
            </div>
          ) : null}
          {showMyDataTransfers ? (
            myDataTransfer?.totalCount > 0 ? (
              <>
                <div className={Styles.dataTransferSection}>
                  <h4>{`My Data Transfers ( ${myDataTransfer?.totalCount} / ${allDataTransfer?.totalCount} )`}</h4>
                </div>
                <div className={classNames(Styles.allDataproductCardviewContent)}>
                  {myDataTransfer?.records?.map((product, index) => {
                    return <DataTranferCardLayout key={index} product={product} user={user} isDataProduct={true} />;
                  })}
                </div>
              </>
            ) : (
              <div className={Styles.dataTransferSection}>You have not requested access for the data product</div>
            )
          ) : null}

          {!showMyDataTransfers ? (
            <div className={classNames(Styles.allDataproductCardviewContent)}>
              {allDataTransfer?.records?.map((product, index) => {
                return <DataTranferCardLayout key={index} product={product} user={user} isDataProduct={true} />;
              })}
            </div>
          ) : null}
        </div>
      </div>
      {!isCreator ? (
        <div
          className={classNames(
            'accessBtnSet',
            !selectedDataProduct.isPublish ? 'indraft' : '',
            myDataTransfer?.totalCount === 0 ? 'nomargin' : 'hasmargin',
          )}
        >
          {showHowToAccessModal ? (
            <button className="btn btn-tertiary" type="button" onClick={() => setShowInfoModal(true)}>
              How to access
            </button>
          ) : null}
          <button
            className={classNames(!selectedDataProduct.isPublish ? 'btn indraft' : 'btn btn-tertiary')}
            disabled={!selectedDataProduct.isPublish}
            type="button"
            onClick={() => setShowRequestAccessModal(true)}
          >
            Request access
          </button>
        </div>
      ) : null}
      {!isCreator && !selectedDataProduct.isPublish ? (
        <div className="accessRequestInfo">
          <span>
            <i className="icon mbc-icon info" />
          </span>
          Unable to Request Access as Data Product is in Draft state.
        </div>
      ) : null}
      {showInfoModal && (
        <InfoModal
          title="How to access data"
          show={showInfoModal}
          customHeader={infoModalHeaderContent}
          content={infoModalContent}
          onCancel={() => {
            setShowInfoModal(false);
          }}
        />
      )}
      {showRequestAccessModal && (
        <Modal
          title="Request access"
          show={showRequestAccessModal}
          showAcceptButton={false}
          showCancelButton={false}
          scrollableContent={true}
          modalWidth={'90%'}
          content={requestAccessModalContent}
          onCancel={() => {
            setShowRequestAccessModal(false);
          }}
        />
      )}
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteDataProductContent}
        onCancel={deleteDataProductClose}
        onAccept={deleteDataProductAccept}
      />
    </div>
  );
};

export default withRouter(Summary);
