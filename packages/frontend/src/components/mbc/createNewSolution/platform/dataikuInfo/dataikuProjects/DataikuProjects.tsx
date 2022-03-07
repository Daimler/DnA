import cn from 'classnames';
import React, { useState, useEffect } from 'react';
import { ApiClient } from '../../../../../../services/ApiClient';
import { IDataiku, IGetDataikuResult } from '../../../../../../globals/types';
import Styles from './DataikuProjects.scss';
import { SUPPORT_EMAIL_ID } from '../../../../../../globals/constants';

const classNames = cn.bind(Styles);

export interface IDataikuProjectsProps {
  currSolutionId: string;
  onProjectSelection?: (project: IDataiku) => void;
  showError: boolean;
}

const DataikuProjects = (props: IDataikuProjectsProps) => {
  const [dataikuProjects, setDataikuProjects] = useState<IDataiku[]>();
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedProject, setSelectedProject] = useState<IDataiku>(null);

  const getDataikuLiveProjects = () => {
    ApiClient.getDataikuProjectsList(true).then((res: IGetDataikuResult) => {
      if (Array.isArray(res)) {
        setDataikuProjects([]);
      } else {
        setDataikuProjects(
          res.data.filter(
            (item: IDataiku) =>
              item.isProjectAdmin && (item.solutionId === null || item.solutionId === props.currSolutionId),
          ),
        );
      }
    });
  };

  useEffect(() => {
    setDataikuProjects(null);
    getDataikuLiveProjects();
  }, []);

  const onSearchInputChange = (event: React.FormEvent<HTMLInputElement>) => {
    setSearchTerm(event.currentTarget.value);
  };

  const onProjectSelect = (project: IDataiku) => {
    return () => {
      setSelectedProject(project);
      props.onProjectSelection(project);
    };
  };

  return (
    <React.Fragment>
      {dataikuProjects && dataikuProjects.length ? (
        <div className={Styles.projectListPanel}>
          <p>Please select the Dataiku project that you want to link to the solution.</p>
          <div className={Styles.searchPanel}>
            <input
              type="text"
              className={classNames(Styles.searchInputField)}
              placeholder="Search Project"
              onChange={onSearchInputChange}
              maxLength={200}
              value={searchTerm}
            />
            {/* <button className={Styles.clearBtn}>
              <i className="icon mbc-icon close thin" />
            </button> */}
            <button>
              <i className="icon mbc-icon search" />
            </button>
          </div>
          <ul className={classNames('list-item-group divider mbc-scroll', Styles.projectList)}>
            {dataikuProjects
              .filter((project: IDataiku) => project.name.toLowerCase().includes(searchTerm))
              .map((item: IDataiku, index: number) => {
                return (
                  <li
                    className={classNames(
                      'list-item',
                      selectedProject?.projectKey === item.projectKey ? Styles.active : '',
                    )}
                    key={'dnadataiku' + index}
                    onClick={onProjectSelect(item)}
                  >
                    <div className="item-text-wrap">
                      <h6 className="item-text-title">{item.name}</h6>
                      <label className="item-text">{item.shortDesc}</label>
                    </div>
                  </li>
                );
              })}
          </ul>
          {props.showError && <p className="error-message">Please select the dataiku project.</p>}
        </div>
      ) : dataikuProjects === null ? (
        <div className="text-center">
          <div className="progress infinite" />
        </div>
      ) : (
        <div>
          There is no live project available, please create a project. In order to create a new Dataiku Live project,
          please send an email to <a href={'mailto:' + SUPPORT_EMAIL_ID}>{SUPPORT_EMAIL_ID}</a>
          {props.showError && (
            <p className="error-message">You dont have any live dataiku project. Please create the dataiku project.</p>
          )}
        </div>
      )}
    </React.Fragment>
  );
};

export default DataikuProjects;
