import classnames from 'classnames';
import React, { useState } from 'react';
import Styles from './forecast-run-row.scss';
// import from DNA Container
import CircularProgressBar from '../../circularProgressBar/CircularProgressBar';
import ContextMenu from '../../contextMenu/ContextMenu';
import { regionalDateAndTimeConversionSolution } from '../../../utilities/utils';
import Notification from '../../../common/modules/uilab/js/src/notification';
import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import { Envs } from '../../../utilities/envs';
import { chronosApi } from '../../../apis/chronos.api';

const classNames = classnames.bind(Styles);

const ForecastRunRow = (props) => {
  const item = props.item;

  const [showContextMenu, setShowContextMenu] = useState(false);

  const handleShowContextMenu = (value) => {
    setShowContextMenu(value);
  }
  const onRowClick = () => {
    props.openDetails(item);
  };

  const onItemDelete = () => {
    props.showDeleteConfirmModal(props.item);
  };

  const onBrowseClick = () => {
    if(props.item.resultFolderPath) {
      const resultFolderPath = item.resultFolderPath.split('/');
      window.open(`${Envs.STORAGE_MFE_APP_URL}/explorer/${resultFolderPath[0]}/${resultFolderPath[2]}`);
    } else {
      Notification.show('No folder path available for the given run', 'alert');
    }
  }

  const downloadPrediction = () => {
    ProgressIndicator.show();
    if(props.item.resultFolderPath) {
      const resultFolderPath = item.resultFolderPath.split('/');
      chronosApi.getFile(`${resultFolderPath[0]}`, `${resultFolderPath[2]}`, 'y_pred.csv').then((res) => {
        let csvContent = "data:text/csv;charset=utf-8," + res.data;
        let encodedUri = encodeURI(csvContent);
        let link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", "y_pred.csv");
        document.body.appendChild(link);
        link.click();
        link.remove();
        ProgressIndicator.hide();
      }).catch(() => {
        Notification.show('Unable to download the file', 'alert');
        ProgressIndicator.hide();
      });
    }
  }

  const contextMenuItems = [
    {
      title: 'Delete Run/Results',
      onClickFn: onItemDelete
    },
    {
      title: 'Browse in Storage',
      onClickFn: onBrowseClick,
    },
    {
      title: 'Download prediction as .csv',
      onClickFn: downloadPrediction,
    }
  ];

  const handleStatusClick = (e, item) => {
    e.stopPropagation();
    props.onOpenErrorModal(item);
  }

  return (
    <React.Fragment>
      <tr
        key={item.id}
        onClick={showContextMenu ? undefined : onRowClick}
        className={classNames('data-row', Styles.dataRow)}
      >
        <td>
          {/* { item.new && <span className={Styles.badge}>New</span> }  */}
          { item.comment === '' && <span>{item.runName}</span> }
          { item.comment !== '' && <span tooltip-data={item.comment}>{item.runName}</span> }
        </td>
        <td>
          {item.state.result_state === 'SUCCESS' && <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} tooltip-data={item.state.result_state} />}
          {item.state.result_state === 'CANCELED' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.result_state === 'FAILED' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.result_state === 'TIMEDOUT' && <i className={classNames('icon mbc-icon close circle', Styles.closeCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Error'} />}
          {item.state.result_state === 'WARNINGS' && <i className={classNames('icon mbc-icon alert circle', Styles.alertCircle)}  onClick={(e) => handleStatusClick(e, item)} tooltip-data={'Click to View the Warning'} />}
          {item.state.result_state === null && <div tooltip-data={'IN PROGRESS'} ><CircularProgressBar /></div>}
        </td>
        <td>
          {regionalDateAndTimeConversionSolution(item.triggeredOn)}
        </td>
        <td>
          {item.triggeredBy}
        </td>
        <td>
          {item.inputFile.split("/")[2]}
        </td>
        <td>
          {item.forecastHorizon}
        </td>
        <td>
          {item.state.result_state === null ? '...' : item.exogenData ? 'Yes' : 'No'}
        </td>
        <td>
          {item.hierarchy === '' || item.hierarchy === undefined ? 'No Hierarchy' : item.hierarchy}
        </td>
        <td>
          {item.state.result_state !== null && <ContextMenu id={item.id} items={contextMenuItems} isMenuOpen={handleShowContextMenu} />}
        </td>
      </tr>
    </React.Fragment>
  );
};

export default ForecastRunRow;
