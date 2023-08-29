import classNames from 'classnames';
import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import Styles from './input-files.scss';
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import { chronosApi } from '../../apis/chronos.api';
import Modal from 'dna-container/Modal';
import AceEditor from 'react-ace';
import Select from '../../assets/modules/uilab/js/src/select';
//import theme
import 'ace-builds/src-noconflict/theme-solarized_dark';
import 'ace-builds/src-noconflict/mode-yaml';
import { getProjectDetails } from '../../redux/projectDetails.services';

const InputFiles = ({inputFiles, showModal, addNew}) => {
  const { id: projectId } = useParams();
  const isValidFile = (file) => ['yml', 'yaml'].includes(file?.name?.slice((file?.name?.lastIndexOf(".") - 1 >>> 0) + 2));

  const [showPreview, setShowPreview] = useState(false);
  const [blobURL, setBlobUrl] = useState();
  const [selectedConfigFile, setSelectedConfigFile] = useState();

  const project = useSelector(state => state.projectDetails);
  const dispatch = useDispatch();

  const handleUploadFile = (file) => {
    const formData = new FormData();
    formData.append('configFile', file);
    ProgressIndicator.show();
    chronosApi.uploadProjectConfigFile(project?.data?.id, formData).then(() => {
        Notification.show('File uploaded successfully');
        dispatch(getProjectDetails(projectId));
        ProgressIndicator.hide();
        Select.defaultSetup();
      }).catch(error => {
        ProgressIndicator.hide();
        Notification.show(
          error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while uploading config file',
          'alert',
        );
      });
  }

  const handlePreviewFile = (file) => {
    if(addNew) {
      setSelectedConfigFile(file);
      ProgressIndicator.show();
      chronosApi.getProjectConfigFileById(project?.data?.id, file.id).then((res) => {
          setBlobUrl(res.data.configFileData);
          setShowPreview(true);
          ProgressIndicator.hide();
        }).catch(error => {
          ProgressIndicator.hide();
          Notification.show(
            error?.response?.data?.errors?.[0]?.message || error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while fetching preview file data',
            'alert',
          );
        });
    }
  }
  
  return (
    <>
    <div className={Styles.firstPanel}>
      { inputFiles.length > 0 ? 
      <>
        <table className={classNames('ul-table')}>
          <thead>
            <tr className="header-row">
              <th><label>Name</label></th>
              <th><label>Uploaded By</label></th>
              <th><label>Uploaded On</label></th>
              <th>&nbsp;</th>
            </tr>
          </thead>
          <tbody>
            { inputFiles.map(inputFile =>
                <tr className={classNames('data-row', Styles.dataRow)} key={inputFile?.id} onClick={() => handlePreviewFile(inputFile) }>
                  <td>{inputFile?.name}</td>
                  <td>{inputFile?.createdBy}</td>
                  <td>{regionalDateAndTimeConversionSolution(inputFile?.createdOn)}</td>
                  <td><i onClick={(e) => { e.stopPropagation(); showModal(inputFile?.id) }} className={classNames('icon delete', Styles.deleteIcon)} /></td>
                </tr>
              )
            }
          </tbody>
        </table>
      </> :
        <p>No input files present</p>
      }
      { addNew && 
        <div>
          <input type="file" id="fileConfig" name="fileConfig" className={Styles.fileInput} 
            onChange={
              (e) => {
                const isValid = isValidFile(e.target.files[0]);
                if (!isValid) {
                  Notification.show('File is not valid. Only .yml or .yaml files allowed.', 'alert');
                } else {
                  handleUploadFile(e.target.files[0]);
                }
              }
            }
            accept=".yml, .yaml"
          />
          <label htmlFor="fileConfig" className={Styles.selectExisitingFiles}><i className="icon mbc-icon plus" /> Upload new Config File</label>
        </div>
      }
    </div>
    {showPreview && (
      <Modal
        title={`Preview - ${selectedConfigFile.name}`}
        onCancel={() => setShowPreview(false)}
        modalWidth={'80vw'}
        showAcceptButton={false}
        showCancelButton={false}
        show={showPreview}
        content={
          <AceEditor
            width="100%"
            name="storagePreview"
            mode={'yaml'}
            theme="solarized_dark"
            fontSize={16}
            showPrintMargin={false}
            showGutter={false}
            highlightActiveLine={false}
            value={blobURL}
            readOnly={true}
            style={{
              height: '65vh',
            }}
            setOptions={{
              useWorker: false,
              showLineNumbers: false,
              tabSize: 2,
            }}
          />
        }
      />
    )}
    </>
  );
}

export default InputFiles;