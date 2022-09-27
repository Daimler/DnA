import classNames from 'classnames';
import React, { useEffect, useState } from 'react';

import Styles from './styles.scss';
import { useFormContext } from 'react-hook-form';
import { useParams } from 'react-router-dom';

// Container components
import SelectBox from 'dna-container/SelectBox';
import Modal from 'dna-container/Modal';

import { regionalDateAndTimeConversionSolution } from '../../../Utility/utils';

import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../../common/modules/uilab/js/src/notification';
import Tooltip from '../../../common/modules/uilab/js/src/tooltip';
import { Link } from 'react-router-dom';

import IconUpload from '../../../assets/icon_upload.png';
import { chronosApi } from '../../../apis/chronos.api';

const SelectedFile = ({ selectedFile, setSelected }) => {
  return (
    <>
      <div className={Styles.selectedFile}>
        <div>
          <span>Input File</span>
          <span>{selectedFile.name}</span>
        </div>
        <span>
          <i className={classNames('icon mbc-icon check circle', Styles.checkCircle)} />
          <span>File is ready to use.</span>
        </span>
        <span>
          <i onClick={() => setSelected(false)} className={classNames('icon delete', Styles.deleteIcon)} />
        </span>
      </div>
    </>
  );
};

const RunForecast = ({ savedFiles }) => {
  const { id: projectId } = useParams();
  
  const {
    register,
    formState: { errors, isSubmitting },
    handleSubmit,
    trigger,
    // reset,
  } = useFormContext();
  const [keepExistingFiles, setKeepExistingFiles] = useState(false);

  const [showExistingFiles, setShowExistingFiles] = useState(false);

  const [expertView, setExpertView] = useState(false);

  const isValidFile = (file) => ['csv', 'xlsx'].includes(file?.name?.split('.')[1]);

  const frequencyTooltipContent =
    'Please select a frequency for your data in the field below.\n Make sure the datetimes in the first column of the data you upload matches the frequency selected here.\n If your data has no inherent frequency or the frequency is not available in the list, select "No frequency".\n In this case, the first column of your data should contain sortable indices like [1, 2, 3...].';
  const forecastHorizonTooltipContent =
    'Select how many data points in the future the forecast should predict.\n Note that this number should not be more than 1/5th the length of your existing data, ideally less.\n Also, forecasting gets less precise over time, so try to not predict too many points in the future.';

  useEffect(() => {
    SelectBox.defaultSetup();
    Tooltip.defaultSetup();
    //eslint-disable-next-line
  }, []);

  const [selectedInputFile, setSelectedInputFile] = useState();
  const selectedSavedFile = (e) => {
    const selectedOne = savedFiles.filter(item => item.path === e.target.value);
    const selectedInput = {...selectedOne[0]};
    setSelectedInputFile(selectedInput);
  }
  const setSelectedInput = (value) => {
    setIsSelectedFile(value);
    setSelectedInputFile({});
  }

  const [isSelectedFile, setIsSelectedFile] = useState(false);

  const existingFilesContent = (
    <div className={Styles.existingFilesContainer}>
      <div className={Styles.flexLayout}>
        {' '}
        <div className={classNames(`input-field-group include-error ${errors?.savedInputPath?.message ? 'error' : ''}`)}>
          <label id="savedInputPathLabel" htmlFor="existingFilenField" className="input-label">
            Input File <sup>*</sup>
          </label>
          <div className="custom-select" onBlur={() => trigger('savedInputPath')}>
            <select
              id="savedInputPathField"
              required={true}
              required-error={'*Missing entry'}
              {...register('savedInputPath', {
                // validate: (value) => value !== '0' || '*Missing entry',
                onChange: (e) => { selectedSavedFile(e) }
              })}
            >
              <option id="savedInputPathOption" value={0}>
                Choose
              </option>
              {savedFiles?.map((file) => (
                <option id={file.id} key={file.id} value={file.path}>
                  {file.name}
                </option>
              ))}
            </select>
          </div>
          <span className={classNames('error-message')}>{errors?.savedInputPath?.message}</span>
        </div>
      </div>
      {
        selectedInputFile?.name !== undefined &&
          <>
            <p>{selectedInputFile?.name}</p>
            <div className={Styles.flexLayout}>
              <div>
                <div className={Styles.uploadInfo}>
                  <span>Uploaded On</span>
                  <span>{regionalDateAndTimeConversionSolution(selectedInputFile?.createdOn)}</span>
                </div>
                <div className={Styles.uploadInfo}>
                  <span>Uploaded By</span>
                  <span>{selectedInputFile?.createdBy}</span>
                </div>
              </div>
            </div>
          </>
      }
      <hr />
      <div className={Styles.btnContinue}>
        <button
          className="btn btn-primary"
          type="submit"
          disabled={isSubmitting}
          onClick={() => {
            setShowExistingFiles(false);
            setIsSelectedFile(true);
          }}
        >
          Continue with file
        </button>
      </div>
    </div>
  );

  const onDrop = (e) => {
    console.log('Dropped files', e.dataTransfer.files);
    const file = e.dataTransfer.files?.[0];
    const isValid = isValidFile(file);
    if (!isValid) Notification.show('File is not valid.', 'alert');
    setIsSelectedFile(true);
  };

  const onFileDrop = (e) => {
    e.preventDefault();
    if (e.type === 'drop') {
      onDrop?.(e);
    }
  };

  const onSave = (data) => {
    const formData = new FormData();
    formData.append("file", data.file[0]);
    formData.append("runName", data.runName);
    formData.append("configurationFile", data.configurationFile);
    formData.append("frequency", data.frequency);
    formData.append("forecastHorizon", data.forecastHorizon);
    formData.append("comment", data.comment);
    formData.append("saveRequestPart", keepExistingFiles);
    formData.append("savedInputPath", null); // todo file path
    ProgressIndicator.show();
    chronosApi.createForecastRun(formData, projectId).then((res) => {
        console.log(res);
        ProgressIndicator.hide();
      }).catch(error => {
        console.log(error.message);
        ProgressIndicator.hide();
      });
  }

  return (
    <>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <h3>Input File</h3>
          <div className={Styles.infoIcon}>
            <i className="icon mbc-icon info" onClick={() => {}} />
          </div>
          <div className={Styles.formWrapper}>
            <div>
              <p>
                Please upload your Input File and make sure it&apos;s structured according to our{' '}
                <Link to="help">forecasting guidelines</Link>.
              </p>
              <p>
                For a quick start you can download the default template (.xlsx) <a href="#/">right here</a>.
              </p>
            </div>
            {!isSelectedFile ? (
              <div className={Styles.container}>
                <div
                  onDrop={onFileDrop}
                  onDragOver={onFileDrop}
                  onDragLeave={onFileDrop}
                  className={classNames('upload-container', Styles.uploadContainer)}
                >
                  <input type="file" id="file" name="file" {...register('file', { required: '*Missing entry', onChange: (e) => { setIsSelectedFile(true); console.log(e.target.value) }})} />
                  <div className={Styles.rcUpload}>
                    <div className={Styles.dragDrop}>
                      <div className={Styles.icon}>
                        <img src={IconUpload} />
                      </div>
                      <h4>Drag & Drop your Input File here to upload</h4>
                    </div>
                    <div className={Styles.helperTextContainer}>
                      <div className={Styles.browseHelperText}>
                        You can also <label htmlFor="file" className={Styles.selectExisitingFiles}>browse local files</label> (.xlsx)
                      </div>
                      <div
                        className={Styles.browseHelperText}
                        onClick={(e) => {
                          e.stopPropagation();
                          setShowExistingFiles(true);
                        }}
                      >
                        <p>
                          or<button className={Styles.selectExisitingFiles}>select an existing file</button>to run
                          forecast
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <SelectedFile selectedFile={selectedInputFile} setSelected={setSelectedInput} />
            )}
            <div className={Styles.checkbox}>
              <label className="checkbox">
                <span className="wrapper">
                  <input
                    value={keepExistingFiles}
                    type="checkbox"
                    className="ff-only"
                    onChange={() => {
                      setKeepExistingFiles(!keepExistingFiles);
                    }}
                    checked={keepExistingFiles}
                  />
                </span>
                <span className="label">Keep file for future use</span>
              </label>
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.wrapper}>
        <div className={Styles.firstPanel}>
          <h3>Run Parameters</h3>
          <div className={Styles.infoIcon}>
            <label className="switch">
              <span className="label" style={{ marginRight: '5px' }}>
                Enable Expert View
              </span>
              <span className="wrapper">
                <input
                  value={expertView}
                  type="checkbox"
                  className="ff-only"
                  onChange={() => setExpertView(!expertView)}
                  checked={expertView}
                />
              </span>
            </label>
          </div>
          <div className={Styles.formWrapper}>
            <div className={Styles.flexLayout}>
              <div className={classNames('input-field-group include-error', errors.runName ? 'error' : '')}>
                <label id="runNameLabel" htmlFor="runNameInput" className="input-label">
                  Run Name <sup>*</sup>
                </label>
                <input
                  {...register('runName', { required: '*Missing entry' })}
                  type="text"
                  className="input-field"
                  id="runNameInput"
                  maxLength={200}
                  placeholder="eg. YYYY-MM-DD_run-topic"
                  autoComplete="off"
                />
                <span className={classNames('error-message')}>{errors.runName?.message}</span>
              </div>
              <div className={Styles.configurationContainer}>
                <div
                  className={classNames(
                    `input-field-group include-error ${errors?.configurationFile?.message ? 'error' : ''}`,
                  )}
                >
                  <label id="configurationLabel" htmlFor="configurationField" className="input-label">
                    Configuration File <sup>*</sup>
                  </label>
                  <div className="custom-select" onBlur={() => trigger('configurationFile')}>
                    <select
                      id="configurationField"
                      required={true}
                      required-error={'*Missing entry'}
                      {...register('configurationFile', {
                        validate: (value) => value !== '0' || '*Missing entry',
                      })}
                    >
                      <option id="configurationOption" value={0}>
                        Choose
                      </option>
                      <option value={'Default-Settings'}>Default Configuration</option>
                    </select>
                  </div>
                  <span className={classNames('error-message')}>{errors?.configurationFile?.message}</span>
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div className={Styles.frequencyContainer}>
                <div
                  className={classNames(
                    `input-field-group include-error ${errors?.frequency?.message ? 'error' : ''}`,
                    Styles.tooltipIcon,
                  )}
                >
                  <label id="frequencyLabel" htmlFor="frequencyField" className="input-label">
                    Frequency <sup>*</sup>
                    <i className="icon mbc-icon info" tooltip-data={frequencyTooltipContent} />
                  </label>
                  <div className="custom-select" onBlur={() => trigger('frequency')}>
                    <select
                      id="frequencyField"
                      required={true}
                      required-error={'*Missing entry'}
                      {...register('frequency', {
                        validate: (value) => value !== '0' || '*Missing entry',
                      })}
                    >
                      <option id="frequencyOption" value={0}>
                        Choose
                      </option>
                      <option value={'DAILY'}>Daily</option>
                      <option value={'WEEKLY'}>Weekly</option>
                      <option value={'MONTHLY'}>Monthly</option>
                      <option value={'YEARLY'}>Yearly</option>
                      <option value={'NO_FREQUENCY'}>No Frequency</option>
                    </select>
                  </div>
                  <span className={classNames('error-message')}>{errors?.frequency?.message}</span>
                </div>
              </div>
              <div className={Styles.forecastHorizonContainer}>
                <div className={classNames('input-field-group include-error', errors.forecastHorizon ? 'error' : '', Styles.tooltipIcon)}>
                  <label id="forecastHorizonLabel" htmlFor="forecastHorizonField" className="input-label">
                    Forecast Horizon <sup>*</sup>
                    <i className="icon mbc-icon info" tooltip-data={forecastHorizonTooltipContent} />
                  </label>
                  <input
                    {...register('forecastHorizon', { required: '*Missing entry'})}
                    type="number"
                    className="input-field"
                    id="forecastHorizonField"
                    defaultValue={1}
                    placeholder="eg. 1"
                    autoComplete="off"
                  />
                  <span className={classNames('error-message')}>{errors.forecastHorizon?.message}</span>
                </div>
              </div>
            </div>
            <div>
              <div
                id="comment"
                className={classNames('input-field-group include-error area', errors.comment ? 'error' : '')}
              >
                <label className="input-label" htmlFor="comment">
                  Add comment
                </label>
                <textarea className="input-field-area" type="text" {...register('comment')} rows={50} id="comment" />
                <span className={classNames('error-message')}>{errors?.comment?.message}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="btnContainer">
        <button
          className="btn btn-tertiary"
          type="submit"
          disabled={isSubmitting}
          onClick={handleSubmit(onSave)}
        >
          Run Forecast
        </button>
      </div>
      <Modal
        title={'Select existing input file'}
        showAcceptButton={false}
        showCancelButton={false}
        modalWidth={'35%'}
        buttonAlignment="right"
        show={showExistingFiles}
        content={existingFilesContent}
        scrollableContent={false}
        onCancel={() => {
          setShowExistingFiles(false);
        }}
      />
    </>
  );
};

export default RunForecast;
