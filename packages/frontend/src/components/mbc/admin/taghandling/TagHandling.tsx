import * as React from 'react';
// @ts-ignore
import Notification from '../../../../assets/modules/uilab/js/src/notification';
// @ts-ignore
import ProgressIndicator from '../../../../assets/modules/uilab/js/src/progress-indicator';
import SelectBox from '../../../../components/formElements/SelectBox/SelectBox';
import Pagination from '../../pagination/Pagination';
import Styles from './TagHandling.scss';

import { IFitlerCategory, ITagResult } from '../../../../globals/types';
import { ApiClient } from '../../../../services/ApiClient';
import { ISortField } from '../../allSolutions/AllSolutions';
import { TagRowItem } from './tagrowitem/TagRowItem';

import { SESSION_STORAGE_KEYS } from '../../../../globals/constants';
import ConfirmModal from '../../../formElements/modal/confirmModal/ConfirmModal';

export interface ITagHandlingState {
  algoCategory: IFitlerCategory;
  dataSourceCategory: IFitlerCategory;
  languageCategory: IFitlerCategory;
  platformCategory: IFitlerCategory;
  tagCategory: IFitlerCategory;
  visualizationCategory: IFitlerCategory;
  currentFilterCategory: IFitlerCategory;
  categories: IFitlerCategory[];
  maxItemsPerPage: number;
  totalNumberOfRecords: number;
  currentPageNumber: number;
  currentPageOffset: number;
  totalNumberOfPages: number;
  sortBy: ISortField;
  results: ITagResult[];
  showDeleteTagModal: boolean;
  tagToBeDeleted: ITagResult;
  searchText: string;
  relatedProductList: IFitlerCategory;
}

export class TagHandling extends React.Component<any, ITagHandlingState> {
  constructor(props: any) {
    super(props);
    this.state = {
      algoCategory: { id: 1, name: 'Algorithms' },
      dataSourceCategory: { id: 2, name: 'Data Sources' },
      languageCategory: { id: 3, name: 'Languages' },
      platformCategory: { id: 4, name: 'Platform' },
      tagCategory: { id: 5, name: 'Tags' },
      relatedProductList: { id: 7, name: 'Related Products' },
      visualizationCategory: { id: 6, name: 'Visualization' },
      currentFilterCategory: { id: 0, name: 'Select' },
      categories: [{ id: 0, name: 'Select' }],
      sortBy: {
        name: 'name',
        currentSortType: 'desc',
        nextSortType: 'asc',
      },
      maxItemsPerPage: parseInt(sessionStorage.getItem(SESSION_STORAGE_KEYS.PAGINATION_MAX_ITEMS_PER_PAGE), 10) || 15,
      totalNumberOfRecords: 0,
      totalNumberOfPages: 1,
      currentPageNumber: 1,
      currentPageOffset: 0,
      results: [],
      showDeleteTagModal: false,
      tagToBeDeleted: null,
      searchText: null,
    };
    ApiClient.getDropdownList('categories').then((dropdownList: any) => {
      this.setState({
        categories: this.state.categories.concat(dropdownList.data),
      });
    });
  }

  public getAlgorithms = (results: ITagResult[]) => {
    return ApiClient.getAlgorithms()
      .then((res) => {
        if (res) {
          res.forEach((algo) => {
            results.push({ category: this.state.algoCategory, id: algo.id + '', name: algo.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getDataSources = (results: ITagResult[]) => {
    return ApiClient.getMasterDataSources()
      .then((res1) => {
        if (res1) {
          res1.forEach((ds) => {
            results.push({ category: this.state.dataSourceCategory, id: ds.id + '', name: ds.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getPlatforms = (results: ITagResult[]) => {
    return ApiClient.getPlatforms()
      .then((res1) => {
        if (res1) {
          res1.forEach((platform) => {
            results.push({ category: this.state.platformCategory, id: platform.id + '', name: platform.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public relatedProductList = (results: ITagResult[]) => {
    return ApiClient.relatedProductList()
      .then((res1) => {
        if (res1) {
          res1.forEach((relPrd) => {
            results.push({ category: this.state.relatedProductList, id: relPrd.id + '', name: relPrd.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getLanguages = (results: ITagResult[]) => {
    return ApiClient.getLanguages()
      .then((res1) => {
        if (res1) {
          res1.forEach((language) => {
            results.push({ category: this.state.languageCategory, id: language.id + '', name: language.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getTags = (results: ITagResult[]) => {
    return ApiClient.getTags()
      .then((res1) => {
        if (res1) {
          res1.forEach((tag) => {
            results.push({ category: this.state.tagCategory, id: tag.id, name: tag.name });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public getVisualizations = (results: ITagResult[]) => {
    return ApiClient.getVisualizations()
      .then((res1) => {
        if (res1) {
          res1.forEach((visualization) => {
            results.push({
              category: this.state.visualizationCategory,
              id: visualization.id + '',
              name: visualization.name,
            });
          });
        }
      })
      .catch((error) => {
        this.setState(
          {
            results: [],
          },
          () => {
            this.showErrorNotification(error.message ? error.message : 'Some Error Occured');
          },
        );
      });
  };
  public async getResults() {
    let results: ITagResult[] = [];
    const filterCategory = this.state.currentFilterCategory;
    switch (filterCategory.id) {
      case 0:
        await this.getAlgorithms(results);
        await this.getDataSources(results);
        await this.getLanguages(results);
        await this.getPlatforms(results);
        await this.getTags(results);
        await this.getVisualizations(results);
        await this.relatedProductList(results);
        break;
      case 1:
        await this.getAlgorithms(results);
        break;
      case 2:
        await this.getDataSources(results);
        break;
      case 3:
        await this.getLanguages(results);
        break;
      case 4:
        await this.getPlatforms(results);
        break;
      case 5:
        await this.getTags(results);
        break;
      case 6:
        await this.getVisualizations(results);
        break;
      case 7:
        await this.relatedProductList(results);
        break;
    }
    if (this.state.searchText) {
      results = results.filter((result) => {
        return result.name.toLowerCase().match(this.state.searchText.toLowerCase());
      });
    }
    if (this.state.sortBy) {
      if (this.state.sortBy.name === 'name') {
        results = results.sort((a: ITagResult, b: ITagResult) => {
          if (this.state.sortBy.currentSortType === 'asc') {
            return a.name.toLowerCase() === b.name.toLowerCase() ? 0 : -1;
          } else {
            return a.name.toLowerCase() === b.name.toLowerCase() ? -1 : 0;
          }
        });
      } else if (this.state.sortBy.name === 'category') {
        results = results.sort((a: ITagResult, b: ITagResult) => {
          if (this.state.sortBy.currentSortType === 'asc') {
            return a.category.id - b.category.id;
          } else {
            return -(a.category.id - b.category.id);
          }
        });
      }
    }

    this.setState({
      results: results.slice(
        this.state.currentPageOffset > results.length ? 0 : this.state.currentPageOffset,
        this.state.currentPageOffset + this.state.maxItemsPerPage < results.length
          ? this.state.currentPageOffset + this.state.maxItemsPerPage
          : results.length,
      ),
      totalNumberOfPages: Math.ceil(results.length / this.state.maxItemsPerPage),
      currentPageNumber:
        this.state.currentPageNumber > Math.ceil(results.length / this.state.maxItemsPerPage)
          ? Math.ceil(results.length / this.state.maxItemsPerPage) > 0
            ? Math.ceil(results.length / this.state.maxItemsPerPage)
            : 1
          : this.state.currentPageNumber,
    });
  }
  public async componentDidMount() {
    ProgressIndicator.show();
    await this.getResults();
    SelectBox.defaultSetup();
    ProgressIndicator.hide();
  }

  public onSearchInput = (e: React.FormEvent<HTMLInputElement>) => {
    ProgressIndicator.show();
    const searchText = e.currentTarget.value;
    this.setState({ searchText }, () => {
      this.getResults();
      ProgressIndicator.hide();
    });
  };
  public onCategoryChange = (e: React.FormEvent<HTMLSelectElement>) => {
    const selectedOptions = e.currentTarget.selectedOptions;

    if (selectedOptions.length) {
      Array.from(selectedOptions).forEach((option) => {
        // tslint:disable-next-line: radix
        this.setState(
          {
            currentFilterCategory: { id: parseInt(option.value, 0), name: option.label },
            currentPageOffset: 0,
            currentPageNumber: 1,
          },
          () => {
            this.getResults();
          },
        );
      });
    }
  };
  public sortTags = (propName: string, sortOrder: string) => {
    const sortBy: ISortField = {
      name: propName,
      currentSortType: sortOrder,
      nextSortType: this.state.sortBy.currentSortType,
    };
    this.setState(
      {
        sortBy,
      },
      () => {
        this.getResults();
      },
    );
  };
  public showDeleteConfirmModal = (tagItem: ITagResult) => {
    this.setState({ tagToBeDeleted: tagItem, showDeleteTagModal: true });
  };
  public render() {
    const resultData = this.state.results.map((result) => {
      return (
        <TagRowItem
          tagItem={result}
          key={result.id + '' + result.category.id}
          showDeleteConfirmModal={this.showDeleteConfirmModal}
        />
      );
    });
    const modalContent: React.ReactNode = (
      <div id="contentparentdiv" className={Styles.modalContentWrapper}>
        <div className={Styles.modalTitle}>Delete Tag</div>
        <div className={Styles.modalContent}>
          The tag &laquo;{this.state.tagToBeDeleted ? this.state.tagToBeDeleted.name : ''}&raquo; will be removed
          permanently.
        </div>
      </div>
    );
    return (
      <div className={Styles.mainPanel}>
        <div className={Styles.wrapper}>
          <div className={Styles.searchPanel}>
            <div>
              <div className="input-field-group search-field">
                <label id="searchLabel" className="input-label" htmlFor="searchInput">
                  Search Entries
                </label>
                <input
                  type="text"
                  className="input-field search"
                  required={false}
                  id="searchInput"
                  maxLength={200}
                  placeholder="Type here"
                  autoComplete="off"
                  onChange={this.onSearchInput}
                />
              </div>
            </div>
            <div>
              <div id="statusContainer" className="input-field-group">
                <label id="statusLabel" className="input-label" htmlFor="statusSelect">
                  Filter by
                </label>
                <div className={Styles.customContainer}>
                  <div className="custom-select">
                    <select id="filterBy" onChange={this.onCategoryChange}>
                      {/* {this.state.categories && */}
                      {this.state.categories.map((category: IFitlerCategory) => (
                        <option key={category.id} id={'' + category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                      {/* }  */}
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
          {resultData.length === 0 ? (
            <div className={Styles.tagIsEmpty}>There is no tag available</div>
          ) : (
            <div className={Styles.tablePanel}>
              <table className="ul-table users">
                <thead>
                  <tr className="header-row">
                    <th onClick={this.sortTags.bind(null, 'name', this.state.sortBy.nextSortType)}>
                      <label
                        className={
                          'sortable-column-header ' +
                          (this.state.sortBy.name === 'name' ? this.state.sortBy.currentSortType : '')
                        }
                      >
                        <i className="icon sort" />
                        Name
                      </label>
                    </th>
                    <th onClick={this.sortTags.bind(null, 'category', this.state.sortBy.nextSortType)}>
                      <label
                        className={
                          'sortable-column-header ' +
                          (this.state.sortBy.name === 'category' ? this.state.sortBy.currentSortType : '')
                        }
                      >
                        <i className="icon sort" />
                        Category
                      </label>
                    </th>

                    <th className="actionColumn"><label>Action</label></th>
                  </tr>
                </thead>
                <tbody>{resultData}</tbody>
              </table>
              {this.state.results.length ? (
                <Pagination
                  totalPages={this.state.totalNumberOfPages}
                  pageNumber={this.state.currentPageNumber}
                  onPreviousClick={this.onPaginationPreviousClick}
                  onNextClick={this.onPaginationNextClick}
                  onViewByNumbers={this.onViewByPageNum}
                  displayByPage={true}
                />
              ) : (
                ''
              )}
            </div>
          )}
          <ConfirmModal
            title="Delete Tag"
            acceptButtonTitle="Delete"
            cancelButtonTitle="Cancel"
            showAcceptButton={true}
            showCancelButton={true}
            show={this.state.showDeleteTagModal}
            content={modalContent}
            onCancel={this.onCancellingDeleteChanges}
            onAccept={this.onAcceptDeleteChanges}
          />
        </div>
      </div>
    );
  }
  protected onCancellingDeleteChanges = () => {
    this.setState({ showDeleteTagModal: false });
  };
  protected onAcceptDeleteChanges = () => {
    ProgressIndicator.show();
    const tagToBeDeleted = this.state.tagToBeDeleted;
    const handleSuccess = () => {
      this.setState({ showDeleteTagModal: false }, () => {
        ProgressIndicator.hide();
        this.showNotification('Tag deleted successfully');
        this.getResults();
      });
    };
    const handleFailure = () => {
      ProgressIndicator.hide();
      this.showErrorNotification('Error during tag delete');
    };
    if (tagToBeDeleted.category.id === 1) {
      ApiClient.deleteAlgorithm(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 2) {
      ApiClient.deleteDataSource(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 3) {
      ApiClient.deleteLanguage(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 4) {
      ApiClient.deletePlatform(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 5) {
      ApiClient.deleteTag(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 6) {
      ApiClient.deleteVisualization(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    } else if (tagToBeDeleted.category.id === 7) {
      ApiClient.deleterelatedProductList(tagToBeDeleted.id)
        .then((res) => {
          if (res) {
            handleSuccess();
          }
        })
        .catch((error) => {
          handleFailure();
        });
    }
  };
  protected onPaginationPreviousClick = () => {
    const currentPageNumber = this.state.currentPageNumber - 1;
    const currentPageOffset = (currentPageNumber - 1) * this.state.maxItemsPerPage;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getResults();
    });
  };

  protected onPaginationNextClick = () => {
    let currentPageNumber = this.state.currentPageNumber;
    const currentPageOffset = currentPageNumber * this.state.maxItemsPerPage;
    currentPageNumber = currentPageNumber + 1;
    this.setState({ currentPageNumber, currentPageOffset }, () => {
      this.getResults();
    });
  };

  protected showErrorNotification(message: string) {
    Notification.show(message, 'alert');
  }

  protected showNotification(message: string) {
    Notification.show(message);
  }
  protected onViewByPageNum = (pageNum: number) => {
    const currentPageOffset = 0;
    const maxItemsPerPage = pageNum;
    this.setState({ currentPageOffset, maxItemsPerPage, currentPageNumber: 1 }, () => {
      this.getResults();
    });
  };
}
