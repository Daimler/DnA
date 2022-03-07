import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { ApiClient } from '../../../../services/ApiClient';
import InputFieldsUtils from '../../../formElements/InputFields/InputFieldsUtils';
import SelectBox from '../../../formElements/SelectBox/SelectBox';
import AttachmentUploader from '../AttachmentUploader/AttachmentUploader';

import {
  IAttachment,
  IBusinessGoal,
  IDivision,
  IDivisionAndSubDivision,
  IInfoItem,
  ILocation,
  ILogoDetails,
  IProjectStatus,
  IRelatedProduct,
  ISubDivision,
  ITag,
} from '../../../../globals/types';
import AddRelatedProductModal from './addRelatedProductModal/AddRelatedProductModal';
import Styles from './Description.scss';
import LogoManager from './logoManager/LogoManager';
import Tags from '../../../formElements/tags/Tags';
import { InfoModal } from '../../../formElements/modal/infoModal/InfoModal';
import { Envs } from '../../../../globals/Envs';
import { DataStrategyDomainInfoList, ExistingSolutionInfoList } from '../../../../globals/constants';
import { InfoList } from '../../../formElements/modal/infoModal/InfoList';

const classNames = cn.bind(Styles);

export interface IDescriptionProps {
  locations: ILocation[];
  divisions: IDivision[];
  subDivisions: ISubDivision[];
  projectStatuses: IProjectStatus[];
  tags: ITag[];
  relatedProductsMaster: IRelatedProduct[];
  logoDetails: ILogoDetails;
  attachments: IAttachment[];
  businessGoalsList: IBusinessGoal[];
  description: IDescriptionRequest;
  modfifyDescription: (description: IDescriptionRequest) => void;
  onSaveDraft: (tabToBeSaved: string) => void;
  // onStateChange: () => void;
  isProvision: boolean;
}

export interface IDescriptionState {
  // locations: ILocation[];
  // divisions: IDivision[];
  // projectStatuses: IProjectStatus[];
  subDivisions: ISubDivision[];
  // request: IDescriptionRequest;
  loading: boolean;
  productName: string;
  productNameError: string;
  divisionValue: IDivisionAndSubDivision;
  divisionError: string;
  subDivisionError: string;
  // subDivisionValue: string;
  locationValue: ILocation[];
  relatedProductValue: string[];
  locationError: string;
  statusValue: IProjectStatus;
  businessGoalVal: string[];
  businessGoal: string[];
  businessGoalValError: string;
  statusError: string;
  relatedProductsError: string;
  tags: string[];
  description: string;
  reasonForHoldOrClose: string;
  onHoldError: string;
  descriptionError: string;
  expectedBenefits: string;
  expectedBenefitsError: string;
  businessNeeds: string;
  businessNeedsError: string;
  showTagsMissingError: boolean;
  logoDetails: ILogoDetails;
  attachments: IAttachment[];
  relatedProductObj: IRelatedProduct[];
  showAddRelatedProductModal: boolean;
  showAddNeededRoleModal: boolean;
  onAddRelatedProductModalCancel: boolean;
  chips: string[];
  newRelatedProducts: IRelatedProduct[];
  dataStrategyDomainMaster: IBusinessGoal[];
  dataStrategyDomain: string;
  dataStrategyDomainVal: string;
  dataStrategyDomainValError: string;
  neededRoleMaster: IRelatedProduct[];
  neededRoleObj: IRelatedProduct[];
  newBusinessGoalMaster: IRelatedProduct[];
  numberOfRequestedFTE: number;
  isExistingSolution: boolean;
  showDataStrategyDomainsInfo: boolean;
  showExistingSolutionInfo: boolean;
}

export interface IDescriptionRequest {
  businessGoal: string[];
  productName: string;
  location: ILocation[];
  division: IDivisionAndSubDivision;
  relatedProducts: string[];
  reasonForHoldOrClose: string;
  status: IProjectStatus;
  expectedBenefits: string;
  description: string;
  businessNeeds: string;
  tags: string[];
  logoDetails?: ILogoDetails;
  attachments: IAttachment[];
  businessGoalsList: IBusinessGoal[];
  dataStrategyDomain: string;
  requestedFTECount: number;
  isExistingSolution: boolean;
}

export default class Description extends React.Component<IDescriptionProps, IDescriptionState> {
  public static getDerivedStateFromProps(props: IDescriptionProps, state: IDescriptionState) {
    return {
      productName: props.description.productName,
      locationValue: props.description.location,
      divisionValue: props.description.division,
      subDivisions: state.subDivisions.length ? state.subDivisions : props.subDivisions,
      statusValue: props.description.status,
      businessGoal: props.description.businessGoal,
      description: props.description.description,
      reasonForHoldOrClose: props.description.reasonForHoldOrClose || '',
      status: props.description.status,
      tags: props.description.tags,
      businessNeeds: props.description.businessNeeds,
      division: props.description.division,
      expectedBenefits: props.description.expectedBenefits,
      logoDetails: props.description.logoDetails,
      attachments: props.description.attachments,
      businessGoalsList: props.description.businessGoalsList,
      relatedProducts: props.relatedProductsMaster,
      relatedProductValue: props.description.relatedProducts,
      dataStrategyDomain: props.description.dataStrategyDomain,
      numberOfRequestedFTE: props.description.requestedFTECount,
      isExistingSolution: props.description.isExistingSolution,
    };
  }

  private addRelatedProductModalRef = React.createRef<AddRelatedProductModal>();

  constructor(props: IDescriptionProps) {
    super(props);
    this.state = {
      reasonForHoldOrClose: null,
      subDivisions: [],
      loading: true,
      productName: null,
      productNameError: null,
      divisionValue: null,
      divisionError: null,
      subDivisionError: null,
      // subDivisionValue: null,
      locationValue: [],
      relatedProductValue: [],
      businessGoalVal: [],
      businessGoal: [],
      businessGoalValError: null,
      locationError: null,
      statusValue: null,
      statusError: null,
      relatedProductsError: null,
      tags: [],
      description: null,
      onHoldError: null,
      descriptionError: null,
      businessNeeds: null,
      businessNeedsError: null,
      expectedBenefits: null,
      expectedBenefitsError: null,
      showTagsMissingError: false,
      logoDetails: null,
      attachments: [],
      relatedProductObj: [],
      showAddRelatedProductModal: false,
      showAddNeededRoleModal: false,
      onAddRelatedProductModalCancel: false,
      chips: [],
      newRelatedProducts: [],
      dataStrategyDomain: '',
      dataStrategyDomainVal: '',
      dataStrategyDomainValError: null,
      neededRoleObj: [],
      neededRoleMaster: [],
      newBusinessGoalMaster: [],
      numberOfRequestedFTE: 0,
      isExistingSolution: false,
      showDataStrategyDomainsInfo: false,
      showExistingSolutionInfo: false,
      dataStrategyDomainMaster: [],
    };

    // this.onProductNameOnChange = this.onProductNameOnChange.bind(this);
    // this.onDivisionChange = this.onDivisionChange.bind(this);
    // this.onSubDivisionChange = this.onSubDivisionChange.bind(this);
    // this.onLocationChange = this.onLocationChange.bind(this);
    // this.onStatusChange = this.onStatusChange.bind(this);
    // this.onBenefitChange = this.onBenefitChange.bind(this);
    // this.onBusinessNeedChange = this.onBusinessNeedChange.bind(this);
    // this.onDescChange = this.onDescChange.bind(this);
    // this.onHoldChange = this.onHoldChange.bind(this);
  }

  public onProductNameOnChange = (e: React.FormEvent<HTMLInputElement>) => {
    const productName = e.currentTarget.value;
    const description = this.props.description;
    description.productName = productName;
    // this.props.onStateChange();
    this.setState({
      productName,
    });
  };

  public onDescChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const desc = e.currentTarget.value;
    const description = this.props.description;
    description.description = desc;
    // this.props.onStateChange();
    this.setState({
      description: desc,
    });
  };

  public onHoldChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const reasonForHoldOrClose = e.currentTarget.value;
    const description = this.props.description;
    description.reasonForHoldOrClose = reasonForHoldOrClose;
    // this.props.onStateChange();
    this.setState({
      reasonForHoldOrClose,
    });
  };

  public onBenefitChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const expectedBenefits = e.currentTarget.value;
    const description = this.props.description;
    description.expectedBenefits = expectedBenefits;
    // this.props.onStateChange();
    this.setState({
      expectedBenefits,
    });
  };

  public onBusinessNeedChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const businessNeeds = e.currentTarget.value;
    const description = this.props.description;
    description.businessNeeds = businessNeeds;
    // this.props.onStateChange();
    this.setState({
      businessNeeds,
    });
  };

  public onDivisionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const target = e.target as HTMLSelectElement;
    const description = this.props.description;
    const division: IDivisionAndSubDivision = { id: '0', name: null, subdivision: { id: null, name: null } };
    const selectedOptions = target.selectedOptions;
    // this.props.onStateChange();
    if (selectedOptions.length) {
      division.id = selectedOptions[0].value;
      division.name = selectedOptions[0].label;

      if (division.id !== '0' && division.id !== this.state.divisionValue.id) {
        ProgressIndicator.show();

        ApiClient.getSubDivisions(division.id).then((subDivisions) => {
          if (!subDivisions.length) {
            subDivisions = [{ id: '0', name: 'None' }];
          }
          this.setState(
            {
              subDivisions,
            },
            () => {
              ProgressIndicator.hide();
              SelectBox.defaultSetup();
            },
          );
        });
      }
    }
    description.division = division;
    this.setState({ divisionValue: division });
  };

  public onSubDivisionChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const subDivision: ISubDivision = { id: null, name: null };
    // this.props.onStateChange();
    if (selectedOptions.length) {
      subDivision.id = selectedOptions[0].value;
      subDivision.name = selectedOptions[0].label;
    }
    const description = this.props.description;
    description.division.subdivision = subDivision;
    this.setState({ divisionValue: description.division });
  };

  public onLocationChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: ILocation[] = [];
    // this.props.onStateChange();
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        const location: ILocation = { id: null, name: null, is_row: false };
        location.id = option.value;
        location.name = option.label;
        selectedValues.push(location);
      });
    }
    const description = this.props.description;
    description.location = selectedValues;
    this.setState({ locationValue: selectedValues });
  };

  public onRelatedProductChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        selectedValues.push(option.label);
      });
    }
    // const description = this.props.description;
    // description.relatedProducts = selectedValues;
    this.setState({ relatedProductValue: selectedValues });
  };
  public onNeededRoleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const selectedValues: string[] = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        selectedValues.push(option.label);
      });
    }
  };
  public onRelatedProductChangeUpdate = (data: string[]) => {
    const newRelatedProducts: IRelatedProduct[] = data
      .filter(
        (o1) =>
          !this.props.relatedProductsMaster.some((o2: { name: string }) => o1.toLowerCase() === o2.name.toLowerCase()),
      )
      .map((str) => {
        return {
          id: str,
          name: str,
        };
      });
    this.props.relatedProductsMaster.push(...newRelatedProducts);
    const selectedRelatedProducts: string[] = [];
    data.forEach((o1) => {
      this.props.relatedProductsMaster.forEach((o2) => {
        if (o1.toLowerCase() === o2.name.toLowerCase()) {
          selectedRelatedProducts.push(o2.name);
        }
      });
    });

    const selectedValues: string[] = selectedRelatedProducts;
    const { relatedProductValue } = this.state;
    relatedProductValue.push(...selectedValues);
    this.setState({ relatedProductValue });
    InputFieldsUtils.resetErrors('#relatedProductWrapper'); // reseting the parent filed //

    this.setState(
      {
        newRelatedProducts: this.props.relatedProductsMaster,
      },
      () => {
        SelectBox.defaultSetup(false);
      },
    );
  };

  public onStatusChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const status: IProjectStatus = { id: null, name: null };
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        status.id = option.value;
        status.name = option.label;
      });
    }
    const description = this.props.description;
    description.status = status;
    this.setState({ statusValue: status });
  };
  public onDataStrategyDomainChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    let dataStrategyDomain = '';
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        dataStrategyDomain = option.label;
      });
    }
    const description = this.props.description;
    description.dataStrategyDomain = dataStrategyDomain;
    this.setState({ dataStrategyDomain });
    this.setState({ dataStrategyDomainVal: dataStrategyDomain });
  };

  public onBusinessGoalChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;
    const businessGoal: any = [];
    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        businessGoal.push(option.label);
      });
    }
    const description = this.props.description;
    description.businessGoal = businessGoal;
    this.setState({ businessGoal });
    this.setState({ businessGoalVal: businessGoal });
  };

  public onFTEChange = (e: React.FormEvent<HTMLInputElement>) => {
    const numberOfRequestedFTE = e.currentTarget.value !== null ? parseInt(e.currentTarget.value) : 0;
    const description = this.props.description;
    description.requestedFTECount = numberOfRequestedFTE;
    this.setState({
      numberOfRequestedFTE,
    });
  };

  public onIsExistingSolutionChange = (e: React.FormEvent<HTMLInputElement>) => {
    const isExistingSolution = e.currentTarget.value === 'true' ? true : false;
    const description = this.props.description;
    description.isExistingSolution = isExistingSolution;
    this.setState({
      isExistingSolution,
    });
  };

  public render() {
    const productNameError = this.state.productNameError || '';
    const locationError = this.state.locationError || '';
    const divisionError = this.state.divisionError || '';
    // const subDivisionError = this.state.subDivisionError || '';
    const statusError = this.state.statusError || '';
    // const dataStrategyDomainValError = this.state.dataStrategyDomainValError || '';
    // const relatedProductsError = this.state.relatedProductsError || '';
    const descriptionError = this.state.descriptionError || '';
    const onHoldError = this.state.onHoldError || '';
    const businessNeedsError = this.state.businessNeedsError || '';
    const expectedBenefitsError = this.state.expectedBenefitsError || '';
    const businessGoalValError = this.state.businessGoalValError || '';

    const requiredError = '*Missing entry';

    const locationValues = this.state.locationValue.map((location: ILocation) => {
      return location.id;
    });

    const relatedProductValues = this.state.relatedProductValue
      ? this.state.relatedProductValue.map((relatedProduct: string) => {
          return relatedProduct;
        })
      : [];
    const dataStrategyDomainValue = this.state.dataStrategyDomain;
    const businessGoalValue = this.state.businessGoal;

    const relatedProdcutList: IRelatedProduct[] = this.props.relatedProductsMaster;

    const scrollableContent = document.querySelector('.decriptionSection');
    if (scrollableContent) {
      scrollableContent.scrollTop = 0;
    }

    // const contentForDataStrategyDomainsInfoModal = DataStrategyDomainInfoList.map((info: any, index: number) => {
    //   return <React.Fragment key={index}>
    //     <label>{info.domain}:</label>
    //     <p>{info.description}</p>
    //   </React.Fragment>;
    // });

    const contentForDataStrategyDomainsInfoModal = <InfoList list={DataStrategyDomainInfoList as IInfoItem[]} />;

    const contentForExistingSolutionInfoModal = <InfoList list={ExistingSolutionInfoList as IInfoItem[]} />;

    // Used Data Compliance variable to hide specific to company
    const enableDataStatergyInfo = Envs.ENABLE_DATA_COMPLIANCE;

    return (
      <React.Fragment>
        <div className={classNames(this.props.isProvision && Styles.provisionStyles)}>
          <div className={classNames(Styles.wrapper)}>
            <div className={classNames(Styles.firstPanel, 'decriptionSection', 'mbc-scroll')}>
              {this.props.isProvision ? '' : <h3>Please give a detailed solution description</h3>}
              <div className={classNames(Styles.formWrapper)}>
                <div className={classNames(Styles.flexLayout)}>
                  <div>
                    <div>
                      <div>
                        <div
                          className={classNames(
                            'input-field-group include-error',
                            productNameError.length ? 'error' : '',
                          )}
                        >
                          <label id="productNameLabel" htmlFor="productNameInput" className="input-label">
                            Solution Name<sup>*</sup>
                          </label>
                          <input
                            type="text"
                            className="input-field"
                            required={true}
                            required-error={requiredError}
                            id="productNameInput"
                            maxLength={200}
                            placeholder="Type here"
                            autoComplete="off"
                            onChange={this.onProductNameOnChange}
                            value={this.state.productName}
                            // value={this.state.productName}
                          />
                          <span className={classNames('error-message', productNameError.length ? '' : 'hide')}>
                            {productNameError}
                          </span>
                        </div>
                      </div>
                    </div>
                    <div>
                      <div>
                        <div
                          id="locationContainer"
                          className={classNames('input-field-group include-error', locationError.length ? 'error' : '')}
                        >
                          <label id="locationLabel" className="input-label" htmlFor="locationSelect">
                            Location<sup>*</sup>
                          </label>
                          <div id="location" className="custom-select">
                            <select
                              id="locationSelect"
                              multiple={true}
                              required={true}
                              required-error={requiredError}
                              onChange={this.onLocationChange}
                              value={locationValues}
                            >
                              {this.props.locations.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          <span className={classNames('error-message', locationError.length ? '' : 'hide')}>
                            {locationError}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div>
                      <div
                        id="businessGoalContainer"
                        className={classNames('input-field-group', businessGoalValError.length ? 'error' : '')}
                      >
                        <label id="businessGoalLabel" className="input-label" htmlFor="businessGoalSelect">
                          Business Goals<sup>*</sup>
                        </label>
                        <div id="businessGoal" className=" custom-select">
                          <select
                            id="businessGoalSelect"
                            multiple={true}
                            required={true}
                            required-error={requiredError}
                            onChange={this.onBusinessGoalChange}
                            value={businessGoalValue}
                          >
                            {/* <option id="defaultBusinessGoal" value={0}>
                              Choose
                            </option> */}
                            {this.state.newBusinessGoalMaster.map((obj) => (
                              <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                {obj.name}
                              </option>
                            ))}
                          </select>
                        </div>
                        <span className={classNames('error-message', businessGoalValError.length ? '' : 'hide')}>
                          {businessGoalValError}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div>
                    <div
                      className={classNames(
                        Styles.existingSolution,
                        'input-field-group include-error',
                        productNameError.length ? 'error' : '',
                      )}
                    >
                      <label id="newSolutionLabel" htmlFor="newSolutionInput" className="input-label">
                        Is Existing Solution? &nbsp;
                        <i className="icon mbc-icon info" onClick={this.showExistingSolutionInfoModal} />
                      </label>
                      <div>
                        <label className="radio">
                          <span className="wrapper">
                            <input
                              type="radio"
                              name="isNewSolution"
                              onChange={this.onIsExistingSolutionChange}
                              value="true"
                              checked={this.state.isExistingSolution == true}
                            />
                          </span>
                          <span className="label">Yes</span>
                        </label>
                        <label className="radio">
                          <span className="wrapper">
                            <input
                              type="radio"
                              name="isNewSolution"
                              onChange={this.onIsExistingSolutionChange}
                              value="false"
                              checked={this.state.isExistingSolution == false}
                            />
                          </span>
                          <span className="label">No</span>
                        </label>
                      </div>
                    </div>

                    <div className={Styles.flexLayout}>
                      <div>
                        <div
                          id="divisionContainer"
                          className={classNames('input-field-group include-error', divisionError.length ? 'error' : '')}
                        >
                          <label id="divisionLabel" className="input-label" htmlFor="divisionSelect">
                            Division<sup>*</sup>
                          </label>
                          <div id="division" className="custom-select">
                            <select
                              id="divisionSelect"
                              onChange={this.onDivisionChange}
                              required={true}
                              required-error={requiredError}
                              value={this.state.divisionValue.id}
                            >
                              <option id="divisionDefault" value={0}>
                                Choose
                              </option>
                              {this.props.divisions.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          <span className={classNames('error-message', divisionError.length ? '' : 'hide')}>
                            {divisionError}
                          </span>
                        </div>
                      </div>
                      <div>
                        <div id="subDivisionContainer" className={classNames('input-field-group')}>
                          <label id="subDivisionLable" className="input-label" htmlFor="subDivisionSelect">
                            Sub Division
                          </label>
                          <div id="subDivision" className=" custom-select">
                            <select
                              id="subDivisionSelect"
                              onChange={this.onSubDivisionChange}
                              value={this.state.divisionValue.subdivision.id}
                            >
                              {this.state.subDivisions.some((item) => item.id === '0' && item.name === 'None') ? (
                                <option id="subDivisionDefault" value={0}>
                                  None
                                </option>
                              ) : (
                                <>
                                  <option id="subDivisionDefault" value={0}>
                                    Choose
                                  </option>
                                  {this.state.subDivisions.map((obj) => (
                                    <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                      {obj.name}
                                    </option>
                                  ))}
                                </>
                              )}
                            </select>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div>
                      <div>
                        <div
                          id="statusContainer"
                          className={classNames('input-field-group include-error', statusError.length ? 'error' : '')}
                        >
                          <label id="statusLabel" className="input-label" htmlFor="statusSelect">
                            Status<sup>*</sup>
                          </label>
                          <div id="status" className="custom-select">
                            <select
                              id="statusSelect"
                              required={true}
                              required-error={requiredError}
                              onChange={this.onStatusChange}
                              value={this.state.statusValue.id}
                            >
                              <option id="defaultStatus" value={0}>
                                Choose
                              </option>
                              {this.props.projectStatuses.map((obj) => (
                                <option id={obj.name + obj.id} key={obj.id} value={obj.id}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          <span className={classNames('error-message', statusError.length ? '' : 'hide')}>
                            {statusError}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  {this.props.isProvision ? (
                    ''
                  ) : (
                    <div className={Styles.flexLayout}>
                      <div id="relatedProductWrapper">
                        <div
                          id="relatedProductContainer"
                          className={classNames('input-field-group')}
                          // className={classNames('input-field-group', relatedProductsError.length ? 'error' : '')}
                        >
                          <label id="relatedProductLable" className="input-label" htmlFor="relatedProductSelect">
                            Related Products
                          </label>
                          <div id="relatedProduct" className="custom-select">
                            <select
                              id="relatedProductSelect"
                              multiple={true}
                              required={false}
                              // required-error={requiredError}
                              onChange={this.onRelatedProductChange}
                              value={relatedProductValues}
                            >
                              {relatedProdcutList.map((obj) => (
                                <option id={obj.name} key={obj.name} value={obj.name}>
                                  {obj.name}
                                </option>
                              ))}
                            </select>
                          </div>
                          {/* <span className={classNames('error-message', relatedProductsError.length ? '' : 'hide')}>
                        {relatedProductsError}
                        </span>
                            */}
                          <div>
                            <button
                              className={classNames(Styles.relatedPrdEdit)}
                              onClick={this.showAddRelatedProductModalView}
                            >
                              <i className="icon mbc-icon plus" />
                              &nbsp;
                              <span>Add new related products</span>
                            </button>
                          </div>
                        </div>
                      </div>

                      <div
                        id="dataStrategyDomainContainer"
                        // className={classNames('input-field-group', dataStrategyDomainValError.length ? 'error' : '')}
                        className={classNames('input-field-group')}
                      >
                        <label
                          id="dataStrategyDomainLable"
                          className={classNames('input-label', Styles.dataStrategyLabel)}
                          htmlFor="dataStrategyDomainSelect"
                        >
                          Data Strategy Domains{' '}
                          {enableDataStatergyInfo && (
                            <React.Fragment>
                              (Only for MS)&nbsp;
                              <i className="icon mbc-icon info" onClick={this.showDataStrategyDomainsInfoModal} />
                            </React.Fragment>
                          )}
                        </label>
                        <div id="dataStrategyDomain" className=" custom-select">
                          <select
                            id="dataStrategyDomainSelect"
                            // required={true}
                            // required-error={requiredError}
                            onChange={this.onDataStrategyDomainChange}
                            value={dataStrategyDomainValue}
                          >
                            <option id="defaultBusineesGoal" value={0}>
                              Choose
                            </option>
                            {this.state.dataStrategyDomainMaster.map((obj) => (
                              <option id={obj.name + obj.id} key={obj.id} value={obj.name}>
                                {obj.name}
                              </option>
                            ))}
                          </select>
                        </div>
                        {/* <span className={classNames('error-message', dataStrategyDomainValError.length ? '' : 'hide')}>
                          {dataStrategyDomainValError}
                        </span> */}
                      </div>
                    </div>
                  )}
                </div>

                <div className={this.state.statusValue.id === '0' || this.state.statusValue.id === '1' ? 'hide' : ''}>
                  <div
                    id="holdContainer"
                    className={classNames('input-field-group include-error area', onHoldError.length ? 'error' : '')}
                  >
                    <label id="onHoldLabel" className="input-label" htmlFor="onHoldReason">
                      Reason of "On hold" / "Closed"*<sup>*</sup>
                    </label>
                    <textarea
                      className="input-field-area"
                      required={true}
                      required-error={requiredError}
                      rows={50}
                      id="onHoldReason"
                      onChange={this.onHoldChange}
                      value={this.state.reasonForHoldOrClose}
                    />
                    <span className={classNames('error-message', onHoldError.length ? '' : 'hide')}>{onHoldError}</span>
                  </div>
                </div>

                <div>
                  <div
                    id="descriptionContainer"
                    className={classNames(
                      'input-field-group include-error area',
                      descriptionError.length ? 'error' : '',
                    )}
                  >
                    <label id="descriptionLabel" className="input-label" htmlFor="description">
                      Description:<sup>*</sup>
                    </label>
                    <textarea
                      className="input-field-area"
                      required={true}
                      required-error={requiredError}
                      rows={50}
                      id="description"
                      onChange={this.onDescChange}
                      value={this.state.description}
                    />
                    <span className={classNames('error-message', descriptionError.length ? '' : 'hide')}>
                      {descriptionError}
                    </span>
                  </div>
                </div>

                <div>
                  <div
                    id="businessNeedContainer"
                    className={classNames(
                      'input-field-group include-error area',
                      businessNeedsError.length ? 'error' : '',
                    )}
                  >
                    <label id="businessNeedLabel" className="input-label" htmlFor="businessNeed">
                      Business Need:<sup>*</sup>
                    </label>
                    <textarea
                      className="input-field-area"
                      required={true}
                      required-error={requiredError}
                      rows={50}
                      id="businessNeed"
                      onChange={this.onBusinessNeedChange}
                      value={this.state.businessNeeds}
                    />
                    <span className={classNames('error-message', businessNeedsError.length ? '' : 'hide')}>
                      {businessNeedsError}
                    </span>
                  </div>
                </div>

                <div>
                  <div
                    id="benefitsContainer"
                    className={classNames(
                      'input-field-group include-error area',
                      expectedBenefitsError.length ? 'error' : '',
                    )}
                  >
                    <label id="benefitsLabel" className="input-label" htmlFor="benefits">
                      Expected Benefits:<sup>*</sup>
                    </label>
                    <textarea
                      className="input-field-area"
                      required={true}
                      required-error={requiredError}
                      rows={50}
                      id="benefits"
                      onChange={this.onBenefitChange}
                      value={this.state.expectedBenefits}
                    />
                    <span className={classNames('error-message', expectedBenefitsError.length ? '' : 'hide')}>
                      {expectedBenefitsError}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          {!this.props.isProvision ? (
            <>
              <div id="tagsWrapper" className={classNames(Styles.wrapper)}>
                <div id="tagsPanel" className={classNames(Styles.firstPanel)}>
                  <div id="tagsContainer" className={classNames(Styles.formWrapper, Styles.tagsWrapper)}>
                    <h3 id="tagHeading">Tags</h3>
                    <span id="tagDesc" className={classNames(Styles.textDesc)}>
                      Use tags to make it easier to find your solution for other people
                    </span>
                    <div>
                      <Tags
                        title={'Tags'}
                        max={100}
                        chips={this.state.tags}
                        setTags={this.setTags}
                        isMandatory={false}
                        showMissingEntryError={this.state.showTagsMissingError}
                        {...this.props}
                      />
                    </div>
                  </div>
                </div>
              </div>
              <LogoManager logoDetails={this.state.logoDetails} modifyLogoDetails={this.modifyLogoDetails} />
              <AttachmentUploader attachments={this.state.attachments} modifyAttachments={this.modifyAttachments} />
              <div className="btnConatiner">
                <button className="btn btn-primary" type="button" onClick={this.onDescriptionSubmit}>
                  Save & Next
                </button>
              </div>
              <AddRelatedProductModal
                ref={this.addRelatedProductModalRef}
                showAddRelatedProductModal={this.state.showAddRelatedProductModal}
                relatedProduct={this.state.relatedProductObj}
                onAddRelatedProductModalCancel={this.onAddRelatedProductModalCancel}
                max={100}
                chips={this.state.chips}
                onRelatedProductChangeUpdate={this.onRelatedProductChangeUpdate}
              />

              {this.state.showDataStrategyDomainsInfo && (
                <InfoModal
                  title={'Data Strategy Domains Information'}
                  modalWidth={'35vw'}
                  show={this.state.showDataStrategyDomainsInfo}
                  content={contentForDataStrategyDomainsInfoModal}
                  onCancel={this.onDataStrategyDomainsInfoModalCancel}
                />
              )}
              {this.state.showExistingSolutionInfo && (
                <InfoModal
                  title={'Is Existing Solution?'}
                  modalWidth={'35vw'}
                  show={this.state.showExistingSolutionInfo}
                  content={contentForExistingSolutionInfoModal}
                  onCancel={this.onExistingSolutionInfoModalCancel}
                />
              )}
            </>
          ) : (
            ''
          )}

          {this.props.isProvision && (
            <div className={Styles.descriptionBtnProvition}>
              <button className="btn btn-tertiary" type="button" onClick={this.onDescriptionSubmit}>
                Save Details
              </button>
            </div>
          )}
        </div>
      </React.Fragment>
    );
  }

  public componentDidMount() {
    ApiClient.getDescriptionLovData().then((response) => {
      if (response) {
        this.setState({
          newBusinessGoalMaster: response[0].data,
          dataStrategyDomainMaster: response[1].data, // It is a Business Strategy Domain List
        });
      }
    });
    ApiClient.getSkills().then((response) => {
      if (response) {
        this.setState({
          neededRoleMaster: response,
        });
      }
    });
  }

  protected modifyLogoDetails = (logoDetails: ILogoDetails) => {
    const description = this.props.description;
    description.logoDetails = logoDetails;
    this.setState({
      logoDetails,
    });
  };

  protected modifyAttachments = (attachments: IAttachment[]) => {
    const description = this.props.description;
    description.attachments = attachments;
    this.setState({
      attachments,
    });
  };

  protected onDescriptionSubmit = () => {
    if (this.validateDescriptionForm()) {
      this.props.modfifyDescription(this.props.description);
      this.props.onSaveDraft('description');
    }
  };

  protected validateDescriptionForm = () => {
    let formValid = true;
    const errorMissingEntry = '*Missing entry';
    if (!this.state.productName || this.state.productName === '') {
      this.setState({ productNameError: errorMissingEntry });
      formValid = false;
    }

    if (!this.state.locationValue || !this.state.locationValue.length) {
      this.setState({ locationError: errorMissingEntry });
      formValid = false;
    }

    if (!this.state.divisionValue || this.state.divisionValue.id === '0') {
      this.setState({ divisionError: errorMissingEntry });
      formValid = false;
    }

    if (!this.state.divisionValue || this.state.divisionValue.subdivision.id === '0') {
      const subdivision = this.state.divisionValue.subdivision;
      subdivision.id = null;
      subdivision.name = null;
      this.setState({
        divisionValue: {
          id: this.state.divisionValue.id,
          name: this.state.divisionValue.name,
          subdivision,
        },
      });
      // this.setState({ subDivisionError: errorMissingEntry });
      // formValid = false;
    }

    if (!this.state.statusValue || this.state.statusValue.id === '0') {
      this.setState({ statusError: errorMissingEntry });
      formValid = false;
    }
    if (!this.state.businessGoal || !this.state.businessGoal) {
      this.setState({ businessGoalValError: errorMissingEntry });
      formValid = false;
    }

    /*if (!this.state.relatedProductValue || this.state.relatedProductValue.length === 0) {
      this.setState({ relatedProductsError: errorMissingEntry });
      formValid = false;
    }*/
    if (!this.state.description || this.state.description === '') {
      this.setState({ descriptionError: errorMissingEntry });
      formValid = false;
    }

    if (
      (this.state.statusValue.id === '4' || this.state.statusValue.id === '5') &&
      (!this.state.reasonForHoldOrClose || this.state.reasonForHoldOrClose === '')
    ) {
      this.setState({ onHoldError: errorMissingEntry });
      formValid = false;
    }

    if (!this.state.businessNeeds || this.state.businessNeeds === '') {
      this.setState({ businessNeedsError: errorMissingEntry });
      formValid = false;
    }

    if (!this.state.expectedBenefits || this.state.expectedBenefits === '') {
      this.setState({ expectedBenefitsError: errorMissingEntry });
      formValid = false;
    }
    // if (!this.props.description.tags || !this.props.description.tags.length) {
    //   this.setState({ showTagsMissingError: true });
    //   formValid = false;
    // }
    setTimeout(() => {
      const anyErrorDetected = document.querySelector('.error');
      anyErrorDetected?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
    return formValid;
  };

  protected clear = () => {
    const selectElemets = document.querySelectorAll('.select-selected');
    // tslint:disable-next-line: prefer-for-of
    for (let i = 0; i < selectElemets.length; i++) {
      selectElemets[i].remove();
    }
  };

  protected setTags = (arr: string[]) => {
    const description = this.props.description;
    description.tags = arr;
    // this.props.onStateChange();
    this.setState({ showTagsMissingError: arr.length === 0 });
  };

  protected showAddRelatedProductModalView = () => {
    this.setState({ showAddRelatedProductModal: true });
  };

  protected onAddRelatedProductModalCancel = () => {
    this.setState({ showAddRelatedProductModal: false }, () => {
      // this.resetTeamsState();
    });
  };

  protected showDataStrategyDomainsInfoModal = () => {
    this.setState({ showDataStrategyDomainsInfo: true });
  };

  protected onDataStrategyDomainsInfoModalCancel = () => {
    this.setState({ showDataStrategyDomainsInfo: false });
  };

  protected showExistingSolutionInfoModal = () => {
    this.setState({ showExistingSolutionInfo: true });
  };

  protected onExistingSolutionInfoModalCancel = () => {
    this.setState({ showExistingSolutionInfo: false });
  };
}
