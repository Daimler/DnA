import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './DataTransfers.style.scss';
import { Link, withRouter } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';

// import from DNA Container
import Pagination from 'dna-container/Pagination';
import { setDataProducts, setPagination } from './redux/dataTransferSlice';
import { GetDataProducts } from './redux/dataTransfer.services';
import DataProductCardItem from './Layout/CardView/DataTransferCardItem';
import DataProductListItem from './Layout/ListView/DataTransferListItem';

const DataProducts = ({ user, history }) => {
  const dispatch = useDispatch();
  const {
    dataProducts,
    pagination: { dataProductListResponse, totalNumberOfPages, currentPageNumber, maxItemsPerPage },
  } = useSelector((state) => state.provideDataTransfers);

  const [cardViewMode, setCardViewMode] = useState(true);
  const [listViewMode, setListViewMode] = useState(false);

  useEffect(() => {
    dispatch(GetDataProducts());
  }, [dispatch]);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = dataProductListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setDataProducts(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = dataProductListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setDataProducts(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(dataProductListResponse?.length / pageNum);
    const modifiedData = dataProductListResponse.slice(0, pageNum);
    dispatch(setDataProducts(modifiedData));
    dispatch(
      setPagination({
        totalNumberOfPages,
        maxItemsPerPage: pageNum,
        currentPageNumber: 1,
      }),
    );
  };

  return (
    <div className={classNames(Styles.mainPanel)}>
      <div className={classNames(Styles.wrapper)}>
        <div className={classNames(Styles.caption)}>
          <h3>Data Transfer Overview</h3>
          <div className={classNames(Styles.listHeader)}>
            <div tooltip-data="Card View">
              <span
                className={cardViewMode ? Styles.iconactive : Styles.iconInActive}
                onClick={() => {
                  setCardViewMode(true);
                  setListViewMode(false);
                }}
              >
                <i className="icon mbc-icon widgets" />
              </span>
            </div>
            <span className={Styles.dividerLine}> &nbsp; </span>
            <div tooltip-data="List View">
              <span
                className={listViewMode ? Styles.iconactive : Styles.iconInActive}
                onClick={() => {
                  setCardViewMode(false);
                  setListViewMode(true);
                }}
              >
                <i className="icon mbc-icon listview big" />
              </span>
            </div>
          </div>
        </div>
        <div>
          <div>
            {dataProducts?.length === 0 ? (
              <div className={classNames(Styles.content)}>
                <div className={Styles.listContent}>
                  <div className={Styles.emptyProducts}>
                    <span>
                      You don&apos;t have any data transfers at this time.
                      <br /> Please create a new one.
                    </span>
                  </div>
                  <div className={Styles.subscriptionListEmpty}>
                    <br />
                    <Link to="datasharing/create">
                      <button className={'btn btn-tertiary'} type="button">
                        <span>Provide new data transferr</span>
                      </button>
                    </Link>
                  </div>
                </div>
              </div>
            ) : (
              <>
                <div className={Styles.allDataproductContent}>
                  <div className={classNames(Styles.allDataproductCardviewContent)}>
                    {cardViewMode ? (
                      <>
                        <div className={Styles.cardViewContainer} onClick={() => history.push('/datasharing/create')}>
                          <div className={Styles.addicon}> &nbsp; </div>
                          <label className={Styles.addlabel}>Provide new data transfer</label>
                        </div>
                        {dataProducts?.map((product, index) => {
                          return <DataProductCardItem key={index} product={product} user={user} />;
                        })}
                      </>
                    ) : null}
                  </div>
                  <div>
                    {listViewMode ? (
                      <>
                        <div className={classNames('ul-table dataproducts', dataProducts?.length === 0 ? 'hide' : '')}>
                          <div
                            className={classNames('data-row', Styles.listViewContainer)}
                            onClick={() => history.push('/datasharing/create')}
                          >
                            <span className={Styles.addicon}> &nbsp; </span>
                            <label className={Styles.addlabel}>Provide new data transfer</label>
                          </div>
                          {dataProducts?.map((product, index) => {
                            return <DataProductListItem key={index} product={product} user={user} />;
                          })}
                        </div>
                      </>
                    ) : null}
                  </div>
                </div>
                {dataProducts?.length ? (
                  <Pagination
                    totalPages={totalNumberOfPages}
                    pageNumber={currentPageNumber}
                    onPreviousClick={onPaginationPreviousClick}
                    onNextClick={onPaginationNextClick}
                    onViewByNumbers={onViewByPageNum}
                    displayByPage={true}
                  />
                ) : null}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
export default withRouter(DataProducts);
