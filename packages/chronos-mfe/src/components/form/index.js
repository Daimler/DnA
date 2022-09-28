import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import Styles from './Form.style.scss';

import { useForm, FormProvider } from 'react-hook-form';
import { Link } from 'react-router-dom';

import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';

// Form Components
import RunForecast from './RunForecast';
import ForecastResults from './ForecastResults';
import ManageConnections from './ManageConnections';
import ProjectDetails from './ProjectDetails';
import Breadcrumb from '../shared/breadcrumb/Breadcrumb';
import { chronosApi } from '../../apis/chronos.api';

const tabs = {
  runForecast: {},
  forecastResults: {},
  manageConnections: {},
  projectDetails: {},
};

const ForecastForm = ({ user }) => {
  const { id: projectId } = useParams();

  const [currentTab, setCurrentTab] = useState('runForecast');
  const methods = useForm();
  // const { formState, reset } = methods;

  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));

  const [project, setProject] = useState();

  useEffect(() => {
    if (user?.roles?.length) {
      Tabs.defaultSetup();
    } else {
      setTimeout(() => {
        Tabs.defaultSetup();
      }, 100);
    }
  }, [user]);

  useEffect(() => {
    getProjectById();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getProjectById = () => {
    ProgressIndicator.show();
    chronosApi.getForecastProjectById(projectId).then((res) => {
      setProject(res);
      ProgressIndicator.hide();
    }).catch(error => {
      console.log(error.message);
      ProgressIndicator.hide();
    });
  };

  const setTab = (e) => {
    setCurrentTab(e.target.id);
  };

  return (
    <FormProvider {...methods}>
      <div className={classNames(Styles.mainPanel)}>
        <Breadcrumb>
          <li><Link to='/'>Chronos Forecasting</Link></li>
          <li>Project Name</li>
        </Breadcrumb>
        <h3 className={classNames(Styles.title)}>Forecasting Project Name</h3>
        <div id="data-product-tabs" className="tabs-panel">
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                <li className={'tab active'}>
                  <a href="#tab-content-1" id="runForecast" ref={elementRef} onClick={setTab}>
                    Run Forecast
                  </a>
                </li>
                <li className={'tab valid'}>
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
                {/* <li className={savedTabs?.includes('manageConnections') ? 'tab valid' : 'tab disabled'}>
                  <a
                    href="#tab-content-3"
                    id="manageConnections"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[2] = ref;
                    }}
                    onClick={setTab}
                  >
                    Manage Connections
                  </a>
                </li> */}
                <li className={'tab valid'}>
                  <a
                    href="#tab-content-4"
                    id="projectDetails"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[3] = ref;
                    }}
                    onClick={setTab}
                  >
                    Project Details
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div className="tabs-content-wrapper">
            <div id="tab-content-1" className="tab-content">
              <RunForecast savedFiles={project?.savedInputs ? project?.savedInputs : []} />
              {/* <RunForecast savedFiles={savedInputs} /> */}
            </div>
            <div id="tab-content-2" className="tab-content">
              {currentTab === 'forecastResults' && (
                <ForecastResults forecastRuns={project?.runs ? project?.runs : []} />
              )}
            </div>
            <div id="tab-content-3" className="tab-content">
              {currentTab === 'manageConnections' && (
                <ManageConnections />
              )}
            </div>
            <div id="tab-content-4" className="tab-content">
              {currentTab === 'projectDetails' && (
                <ProjectDetails project={project} />
              )}
            </div>
          </div>
        </div>
      </div>
      {currentTab !== 'basic-info' && <div className={Styles.mandatoryInfo}>* mandatory fields</div>}
    </FormProvider>
  );
};
export default ForecastForm;
