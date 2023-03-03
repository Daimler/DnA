import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import styles from './project-details-tab.scss';
// import from DNA Container
import TeamMemberListItem from 'dna-container/TeamMemberListItem';
import Modal from 'dna-container/Modal';
// App components
import ChronosProjectForm from '../chronosProjectForm/ChronosProjectForm';
import ChronosAccessDetails from '../chronosAccessDetails/ChronosAccessDetails';
// utils
import { regionalDateAndTimeConversionSolution } from '../../utilities/utils';

const ProjectDetailsTab = ({project}) => {
  const [editProject, setEditProject] = useState(false);
  const [teamMembers, setTeamMembers] = useState([]);

  useEffect(() => {
    if(project.collaborators !== null) {
      const members = project.collaborators.map(member => ({...member, shortId: member.id, userType: 'internal'}));
      setTeamMembers(members);
    }
  }, [project]);

  const teamMembersList = teamMembers?.map((member, index) => {
    return (
      <TeamMemberListItem
        key={index}
        itemIndex={index}
        teamMember={member}
        hidePosition={true}
        hideContextMenu={!editProject}
        showInfoStacked={true}
        showMoveUp={index !== 0}
        showMoveDown={index + 1 !== teamMembers?.length}
      />
    );
  });

  return (
    <React.Fragment>
      <div className={styles.content}>
        <div className={classNames(styles.contextMenu)}>
          <span className={classNames('trigger', styles.contextMenuTrigger)} onClick={() => setEditProject(true)}>
            <i className="icon mbc-icon edit context" />
          </span>
        </div>
        <h3 id="productName">Project Details</h3>
        <div className={styles.firstPanel}>
          <div className={styles.formWrapper}>
            <div className={classNames(styles.flexLayout, styles.threeColumn)}>
              <div id="productDescription">
                <label className="input-label summary">Project Name</label>
                <br />                    
                {project?.name}
              </div>
              <div id="tags">
                <label className="input-label summary">Created on</label>
                <br />
                {project?.createdOn !== undefined && regionalDateAndTimeConversionSolution(project?.createdOn)}
              </div>
              <div id="isExistingSolution">
                <label className="input-label summary">Created by</label>
                <br />
                {project?.createdBy?.firstName} {project?.createdBy?.lastName}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className={styles.content}>
        <h3 id="productName">Collaborators</h3>
        <div className={styles.firstPanel}>
        <div className={styles.collabAvatar}>
          <div className={styles.teamListWrapper}>
            {teamMembers.length === 0 ? <p className={styles.noCollaborator}>No Collaborators</p> : null}
            {teamMembers.length !== 0 ?
              <div className={styles.membersList}>
                {teamMembersList}
              </div> : null
            }
          </div>
        </div>
        </div>
      </div>
      <div className={styles.content}>
        <h3 id="productName">Access Details for Chronos Forecasting</h3>
        <ChronosAccessDetails />
      </div>
      { editProject &&
        <Modal
          title={'Edit Forecasting Project'}
          showAcceptButton={false}
          showCancelButton={false}
          modalWidth={'60%'}
          buttonAlignment="right"
          show={editProject}
          content={<ChronosProjectForm edit={true} project={project} onSave={() => setEditProject(false)} />}
          scrollableContent={false}
          onCancel={() => setEditProject(false)}
          modalStyle={{
            padding: '50px 35px 35px 35px',
            minWidth: 'unset',
            width: '60%',
            maxWidth: '50vw'
          }}
        />
      }
    </React.Fragment>
  );
}
export default ProjectDetailsTab;
