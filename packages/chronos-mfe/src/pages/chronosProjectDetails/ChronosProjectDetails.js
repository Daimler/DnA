import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import Styles from './chronos-project-details.scss';
import SelectBox from 'dna-container/SelectBox';
// App components
import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import RunForecastTab from '../../components/runForecastTab/RunForecastTab';
import ForecastResultsTab from '../../components/forecastResultsTab/ForecastResultsTab';
import ProjectDetailsTab from '../../components/projectDetailsTab/ProjectDetailsTab';
import ComparisonsTab from '../../components/comparisonsTab/ComparisonsTab';
import Breadcrumb from '../../components/breadcrumb/Breadcrumb';
import { getProjectDetails } from '../../redux/projectDetails.services';
import { reset } from '../../redux/chronosFormSlice';
import { getConfigFiles } from '../../redux/chronosForm.services';

const tabs = {
  runForecast: {},
  forecastResults: {},
  projectDetails: {},
  comparisons: {},
};

const ChronosProjectDetails = ({ user }) => {
  const { id: projectId, tabName } = useParams();

  const [currentTab, setCurrentTab] = useState(tabName !== undefined ? tabName : 'runForecast');
  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));

  const projectDetails = useSelector(state => state.projectDetails);
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(getProjectDetails(projectId));
    dispatch(getConfigFiles(projectId));
    SelectBox.defaultSetup();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    projectDetails.isLoading ? ProgressIndicator.show() : ProgressIndicator.hide();
  }, [projectDetails]);

  useEffect(() => {
    if(currentTab === 'runForecast') {
      dispatch(reset());
      dispatch(getConfigFiles(projectId));
      SelectBox.defaultSetup();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentTab]);

  useEffect(() => {
    if (user?.roles?.length) {
      Tabs.defaultSetup();
    } else {
      setTimeout(() => {
        Tabs.defaultSetup();
      }, 100);
    }
  }, [user]);

  const setTab = (e) => {
    const id = e.target.id;
    if (currentTab !== id) {
      setCurrentTab(id);
    }
  };

  const switchTabs = (currentTab) => {
    const tabIndex = Object.keys(tabs).indexOf(currentTab) + 1;
    setCurrentTab(Object.keys(tabs)[tabIndex]);
    elementRef.current[tabIndex].click();
  };

  const switchToComparisonTab = (currentTab) => {
    const tabIndex = Object.keys(tabs).indexOf(currentTab) + 2;
    setCurrentTab(Object.keys(tabs)[tabIndex]);
    elementRef.current[tabIndex].click();
    dispatch(getProjectDetails(projectId));
  };

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <Breadcrumb>
          <li><Link to='/'>Chronos Forecasting</Link></li>
          <li>{projectDetails?.data?.name}</li>
        </Breadcrumb>
        <h3 className={classNames(Styles.title)}>{projectDetails?.data?.name}</h3>
        <div id="data-product-tabs" className="tabs-panel">
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                <li className={classNames('tab', tabName === undefined && 'active', tabName !== undefined && currentTab === tabName && 'active')}>
                  <a href="#tab-content-1" id="runForecast" ref={elementRef} onClick={setTab}>
                    Run Forecast
                  </a>
                </li>
                <li className={classNames('tab', tabName !== undefined && tabName === 'forecastResults' && 'active')}>
                  <a
                    href="#tab-content-2"
                    id="forecastResults"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[1] = ref;
                    }}
                    onClick={setTab}
                  >
                    Forecast Results
                  </a>
                </li>
                <li className={classNames('tab', tabName !== undefined && tabName === 'projectDetails' && 'active')}>
                  <a
                    href="#tab-content-3"
                    id="projectDetails"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[2] = ref;
                    }}
                    onClick={setTab}
                  >
                    Manage Project
                  </a>
                </li>
                <li className={classNames('tab', tabName !== undefined && tabName === 'comparisons' && 'active')}>
                  <a
                    href="#tab-content-4"
                    id="comparisons"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[3] = ref;
                    }}
                    onClick={setTab}
                  >
                    Comparisons
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div className="tabs-content-wrapper">
            <div id="tab-content-1" className="tab-content">
              <RunForecastTab onRunClick={() => switchTabs(currentTab)} />
            </div>
            <div id="tab-content-2" className="tab-content">
              {currentTab === 'forecastResults' ? (
                <ForecastResultsTab onRunClick={() => switchToComparisonTab(currentTab)} />
              ) : null}
            </div>
            <div id="tab-content-3" className="tab-content">
              {currentTab === 'projectDetails' ? (
                <ProjectDetailsTab />
              ) : null}
            </div>
            <div id="tab-content-4" className="tab-content">
              {currentTab === 'comparisons' ? (
                <ComparisonsTab />
              ) : null}
            </div>
          </div>
        </div>
      </div>
      <div className={Styles.mandatoryInfo}>* mandatory fields</div>
    </>
  );
};
export default ChronosProjectDetails;
