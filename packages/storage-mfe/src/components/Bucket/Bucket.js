import classNames from 'classnames';
import React, { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { BucketList } from './BucketList';
import Styles from './Buckets.scss';

// import from DNA Container
import Pagination from 'dna-container/Pagination';

import { bucketActions } from './redux/bucket.actions';

const AllBuckets = () => {
  const dispatch = useDispatch();
  const {
    bucketList,
    pagination: { bucketListResponse, totalNumberOfPages, currentPageNumber, maxItemsPerPage },
  } = useSelector((state) => state.bucket);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = bucketListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch({
      type: 'BUCKET_DATA',
      payload: modifiedData,
    });
    dispatch({
      type: 'SET_PAGINATION',
      payload: {
        currentPageNumber: currentPageNumberTemp,
      },
    });
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = bucketListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch({
      type: 'BUCKET_DATA',
      payload: modifiedData,
    });
    dispatch({
      type: 'SET_PAGINATION',
      payload: {
        currentPageNumber: currentPageNumberTemp,
      },
    });
  };
  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(bucketListResponse?.length / pageNum);
    const modifiedData = bucketListResponse.slice(0, pageNum);
    dispatch({
      type: 'BUCKET_DATA',
      payload: modifiedData,
    });
    dispatch({
      type: 'SET_PAGINATION',
      payload: {
        totalNumberOfPages,
        maxItemsPerPage: pageNum,
        currentPageNumber: 1,
      },
    });
  };

  useEffect(() => {
    dispatch(bucketActions.getBucketList());
  }, [dispatch, maxItemsPerPage]);

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.caption)}>
            <h3>{'My Storage'}</h3>
          </div>
        </div>
        <div className={classNames(Styles.content)}>
          <div>
            <div className={classNames(Styles.listHeader)}>
              <React.Fragment>
                <div className={classNames(Styles.listHeaderContent)}>
                  {bucketList?.length ? (
                    <React.Fragment>
                      <Link to="createBucket">
                        <button className={bucketList === null ? Styles.btnHide : 'btn btn-primary'} type="button">
                          <i className="icon mbc-icon plus" />
                          <span>Create New Bucket</span>
                        </button>
                      </Link>
                    </React.Fragment>
                  ) : null}
                </div>
              </React.Fragment>
            </div>
            <div className={Styles.listContent}>
              {bucketList?.length === 0 ? (
                <>
                  <div className={Styles.subscriptionListEmpty}>
                    <span>You dont have any storage accounts at this time, please create one</span>
                    <br />
                    <Link to="createBucket">
                      <button className={'btn btn-tertiary'} type="button">
                        <span>Create New Bucket</span>
                      </button>
                    </Link>
                  </div>
                </>
              ) : (
                <div className={Styles.subscriptionList}>
                  <BucketList />
                  {bucketList?.length ? (
                    <Pagination
                      totalPages={totalNumberOfPages}
                      pageNumber={currentPageNumber}
                      onPreviousClick={onPaginationPreviousClick}
                      onNextClick={onPaginationNextClick}
                      onViewByNumbers={onViewByPageNum}
                      displayByPage={true}
                    />
                  ) : null}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default AllBuckets;
