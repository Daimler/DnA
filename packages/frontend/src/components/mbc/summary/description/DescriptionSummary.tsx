import { PDFDownloadLink } from '@react-pdf/renderer';
import cn from 'classnames';
import * as React from 'react';
// @ts-ignore
import Button from '../../../../assets/modules/uilab/js/src/button';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import { history } from '../../../../router/History';
import { IDescriptionRequest } from '../../createNewSolution/description/Description';
import AttachmentsListItem from '../datacompliance/attachments/AttachmentsListItems';
import Styles from './DescriptionSummary.scss';

const classNames = cn.bind(Styles);

export interface IDescriptionSummaryProps {
  solutionId: string;
  description: IDescriptionRequest;
  canEdit: boolean;
  bookmarked: boolean;
  onEdit: (solutionId: string) => void;
  onDelete: (solutionId: string) => void;
  updateBookmark: (solutionId: string, isRemove: boolean) => void;
  onExportToPDFDocument: JSX.Element;
}
export interface IDescriptionSummaryState {
  showContextMenu: boolean;
  contextMenuOffsetTop: number;
  contextMenuOffsetRight: number;
  showChangeLogModal: boolean;
}

export default class DescriptionSummary extends React.Component<IDescriptionSummaryProps, IDescriptionSummaryState> {
  protected isTouch = false;
  protected listRowElement: HTMLDivElement;

  constructor(props: any) {
    super(props);
    this.state = {
      showContextMenu: false,
      contextMenuOffsetTop: 0,
      contextMenuOffsetRight: 0,
      showChangeLogModal: false,
    };
  }

  public componentWillMount() {
    document.addEventListener('touchend', this.handleContextMenuOutside, true);
    document.addEventListener('click', this.handleContextMenuOutside, true);
  }

  public componentWillUnmount() {
    document.removeEventListener('touchend', this.handleContextMenuOutside, true);
    document.removeEventListener('click', this.handleContextMenuOutside, true);
  }

  public toggleContextMenu = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState({
      contextMenuOffsetTop: 10,
      contextMenuOffsetRight: 10,
      showContextMenu: !this.state.showContextMenu,
    });
  };
  public onEditSolution = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onEdit(this.props.solutionId);
      },
    );
  };
  public onDeleteSolution = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.onDelete(this.props.solutionId);
      },
    );
  };

  public addToBookmarks = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.updateBookmark(this.props.solutionId, false);
      },
    );
  };

  public removeFromBookmarks = (e: React.FormEvent<HTMLSpanElement>) => {
    e.stopPropagation();
    this.setState(
      {
        showContextMenu: false,
      },
      () => {
        this.props.updateBookmark(this.props.solutionId, true);
      },
    );
  };

  public openChangeLog = (e: React.FormEvent<HTMLSpanElement>) => {
    this.setState({ showChangeLogModal: true });
  };

  public getParsedDate = (strDate: any) => {
    const date = new Date(strDate);
    let dd: any = date.getDate();
    let mm: any = date.getMonth() + 1; // January is 0!

    const yyyy = date.getFullYear();
    if (dd < 10) {
      dd = '0' + dd;
    }
    if (mm < 10) {
      mm = '0' + mm;
    }
    return (dd + '.' + mm + '.' + yyyy).toString();
  };

  public getParsedTime = (strDate: any) => {
    const date = new Date(strDate);
    let hh: any = date.getHours();
    let mm: any = date.getMinutes(); // January is 0!

    if (hh < 10) {
      hh = '0' + hh;
    }
    if (mm < 10) {
      mm = '0' + mm;
    }
    return (hh + ':' + mm).toString();
  };

  public render() {
    const description = this.props.description;
    const chips =
      description.tags && description.tags.length
        ? description.tags.map((chip: any, index: any) => {
            return (
              <div className="chips read-only" key={index}>
                <label className="name">{chip}</label>
              </div>
            );
          })
        : 'N/A';
    const locations: string[] = [];
    description.location.forEach((l) => {
      locations.push(l.name);
    });
    const pdfFileName = description.productName.replace(/[/|\\:*?"<>]/g, '').replace(/ /g, '-');
    return (
      <React.Fragment>
        <div className={classNames(Styles.mainPanel, 'mainPanelSection')}>
          {/* <button className="btn btn-text back arrow" type="submit" onClick={this.goback}>
            Back
          </button> */}
          <div className={Styles.wrapper}>
            <div className={classNames(Styles.contextMenu, this.state.showContextMenu ? Styles.open : '')}>
              <span onClick={this.toggleContextMenu} className={classNames('trigger', Styles.contextMenuTrigger)}>
                <i className="icon mbc-icon listItem context" />
              </span>
              <div
                style={{
                  top: this.state.contextMenuOffsetTop + 'px',
                  right: this.state.contextMenuOffsetRight + 'px',
                }}
                className={classNames('contextMenuWrapper', this.state.showContextMenu ? '' : 'hide')}
              >
                <ul className="contextList">
                  {this.props.bookmarked ? (
                    <li className="contextListItem">
                      <span onClick={this.removeFromBookmarks}>Remove from My Bookmarks</span>
                    </li>
                  ) : (
                    <li className="contextListItem">
                      <span onClick={this.addToBookmarks}>Add to My Bookmarks</span>
                    </li>
                  )}
                  {this.props.canEdit && (
                    <li className="contextListItem">
                      <span onClick={this.onEditSolution}>Edit Solution</span>
                    </li>
                  )}
                  {this.props.canEdit && (
                    <li className="contextListItem">
                      <span onClick={this.onDeleteSolution}>Delete Solution</span>
                    </li>
                  )}
                  <li className="contextListItem">
                    <PDFDownloadLink
                      document={this.props.onExportToPDFDocument}
                      className={Styles.pdfLink}
                      fileName={`${pdfFileName}.pdf`}
                    >
                      {(doc: any) => (doc.loading ? 'Loading...' : 'Export to PDF')}
                    </PDFDownloadLink>
                  </li>
                </ul>
              </div>
            </div>
            <h3 id="productName">{description.productName}</h3>
            <span className={Styles.description}>Solution Summary</span>
            <div className={Styles.firstPanel}>
              <div className={Styles.formWrapper}>
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                  <div id="productDescription">
                    <label className="input-label summary">Description</label>
                    <br />
                    <div className={Styles.solutionDescription}>{description.description}</div>
                  </div>
                  <div id="tags">
                    <label className="input-label summary">Tags</label>
                    <br />
                    <div className={Styles.tagColumn}>{chips}</div>
                  </div>
                  <div id="isExistingSolution">
                    <label className="input-label summary">Register support of additional resources</label>
                    <br />
                    {description.additionalResource ? description.additionalResource : 'N/A'}
                  </div>
                </div>
                <hr className="divider1" />
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                  <div id="division">
                    <label className="input-label summary">Division</label>
                    <br />
                    {description.division.name}
                  </div>
                  <div id="subdivision">
                    <label className="input-label summary">Sub Division</label>
                    <br />
                    {description.division.subdivision.name ? description.division.subdivision.name : 'None'}
                  </div>
                  <div id="locations">
                    <label className="input-label summary">Location</label>
                    <br />
                    {locations.join(', ')}
                  </div>
                </div>
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                  <div id="status">
                    <label className="input-label summary">Status</label>
                    <br />
                    {description.status.name}
                  </div>
                  <div id="relatedProducts">
                    <label className="input-label summary">Related Products</label>
                    <br />
                    {description.relatedProducts
                      ? description.relatedProducts.length > 0
                        ? description.relatedProducts.join(', ')
                        : 'N/A'
                      : 'N/A'}
                  </div>
                  <div id="businessGoal">
                    <label className="input-label summary">Business Goals</label>
                    <br />
                    {description.businessGoal
                      ? description.businessGoal.length > 0
                        ? description.businessGoal.join(', ')
                        : 'N/A'
                      : 'N/A'}
                  </div>
                </div>
                <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                  {/* <div id="neededRoles">
                    <label className="input-label summary">Needed Roles/Skills</label>
                    <br />
                    {description.neededRoles
                      ? description.neededRoles.length > 0
                        ? description.neededRoles.join(', ')
                        : 'N/A'
                      : 'N/A'}
                  </div> */}
                  <div id="dataStrategyDomain">
                    <label className="input-label summary">Data Strategy Domain</label>
                    <br />
                    {description.dataStrategyDomain ? description.dataStrategyDomain : 'N/A'}
                  </div>
                  {/* <div id="numberOfRequestedFTE">
                    <label className="input-label summary">Number of Requested FTE</label>
                    <br />
                    {description.requestedFTECount}
                  </div> */}
                </div>
                <hr className="divider1" />
                <div className={Styles.flexLayout}>
                  <div id="expectedBenefits">
                    <label className="input-label summary">Expected Benefits</label>
                    <br />
                    <div> {description.expectedBenefits}</div>
                  </div>
                  <div id="businessNeeds">
                    <label className="input-label summary">Business Need</label>
                    <br />
                    <div> {description.businessNeeds}</div>
                  </div>
                </div>
                {description.status.id === '4' || description.status.id === '5' ? (
                  <div className={Styles.flexLayout}>
                    <div id="reasonForHoldOrClose">
                      <label className="input-label summary">Reason of "On hold" / "Closed"</label>
                      <br />
                      <div> {description.reasonForHoldOrClose}</div>
                    </div>
                  </div>
                ) : (
                  ''
                )}
                <hr className="divider1" />
                <div className={Styles.flexLayout}>
                  <div id="attachments">
                    <label className="input-label summary">Attachments</label>
                    <br />
                    <br />
                    <div className={classNames(Styles.attachmentSection)}>
                      <AttachmentsListItem attachments={description.attachments} leftMarginZero={true} />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </React.Fragment>
    );
  }

  protected goback = () => {
    history.goBack();
  };

  protected onRiskAssesmentInfoModalCancel = () => {
    this.setState({ showChangeLogModal: false });
  };
  protected handleContextMenuOutside = (event: MouseEvent | TouchEvent) => {
    if (event.type === 'touchend') {
      this.isTouch = true;
    }

    // Click event has been simulated by touchscreen browser.
    if (event.type === 'click' && this.isTouch === true) {
      return;
    }

    const target = event.target as Element;
    const { showContextMenu } = this.state;
    const elemClasses = target.classList;
    const contextMenuWrapper = document.querySelector('.contextMenuWrapper');
    if (
      !target.classList.contains('trigger') &&
      !target.classList.contains('context') &&
      !target.classList.contains('contextList') &&
      !target.classList.contains('contextListItem') &&
      contextMenuWrapper !== null &&
      contextMenuWrapper.contains(target) === false &&
      showContextMenu
    ) {
      this.setState({
        showContextMenu: false,
      });
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
}
