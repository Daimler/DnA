import React from 'react';
import Styles from './ViewRecipe.scss';
import ServiceCard from '../serviceCard/ServiceCard';

// export interface IViewRecipeProps {
//   recipeName: string;
// }

// export interface IRecipeField {
//   createdBy: ICodeCollaborator;
//   createdOn: string;
//   diskSpace: string;
//   maxCpu: string;
//   maxRam: string;
//   minCpu: string;
//   minRam: string;
//   isPublic: boolean,
//   oSName: string;
//   gitPath:string,l
//   gitRepoLoc:string,
//   deployPath:string,
//   recipeName: string;
//   recipeType: string;
//   repodetails: string;
//   software: string[];
// }

const ViewRecipe = ({ recipe, additionalServices }) => {  
  // useEffect(() => {
  //   if (!recipeField?.isPublic && recipeField?.users !== null) {
  //     const members =recipeField?.users.map(member => ({ ...member, shortId: member.id, userType: 'internal' }));
  //     setTeamMembers(members);
  //   }
  // }, [recipeField]);
  
  const convertRam = (value ) => {
    const ramValue = parseInt(value)/1000;
    return ramValue.toString();
  };

  const chips =
    recipe?.software && recipe?.software?.length
        ? recipe?.software?.map((chip) => {
            return (
              <>
                <label className="chips">{chip}</label>&nbsp;&nbsp;
              </>
            );
          })
        : 'N/A';

  return (
    <div className={Styles.container}>
      <div className={Styles.header}>
        <h3>{recipe?.recipeName} - Recipe Details</h3>
        {/* <p>Project and Lean Governance details</p> */}
      </div>
      <div className={Styles.flex}>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Recipe Name</p>
            <p className={Styles.value}>{recipe?.recipeName}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Recipe Type</p>
            <p className={Styles.value}>{recipe?.recipeType}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Git Url</p>
            <p className={Styles.value}>{recipe?.repodetails}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Publicly Available</p>
            <p className={Styles.value}>{recipe?.isPublic ? 'Yes' : 'No'}</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Hardware Configuration</p>
            <p className={Styles.value}>DiskSpace- {recipe?.diskSpace}GB CPU- {recipe?.maxCpu} Ram-{convertRam(recipe?.maxRamq)}GB</p>
          </div>
        </div>
        <div className={Styles.col3}>
          <div className={Styles.projectItem}>
            <p className={Styles.label}>Software</p>
            <p className={Styles.value}>{chips}</p>
          </div>
        </div>
      </div>
      {recipe?.isPublic && 
        <div>
          <div className={Styles.projectItem}>
            {/* <p className={Styles.label}>CI/CD</p> */}
          </div>
          <div className={Styles.flex}>
            <div className={Styles.col3}>
              <div className={Styles.projectItem}>
                <p className={Styles.label}>Git Repository</p>
                <p className={Styles.value}>{recipe?.gitPath}</p>
              </div>
            </div>
            <div className={Styles.col3}>
              <div className={Styles.projectItem}>
                <p className={Styles.label}>Docker File Path</p>
                <p className={Styles.value}>{recipe?.gitRepoLoc}</p>
              </div>
            </div>
            <div className={Styles.col3}>
              <div className={Styles.projectItem}>
                <p className={Styles.label}>Helm File Path</p>
                <p className={Styles.value}>{recipe?.deployPath}</p>
              </div>
            </div>
          </div>
        </div>
      }
      <div>
        <div className={Styles.projectItem}>
          <p className={Styles.label}>Additional Services</p>
        </div>
        <div className={Styles.flex}>
          {additionalServices?.map(service => 
            <div key={service?.serviceName} className={Styles.col3}>
              <ServiceCard service={service} />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ViewRecipe;
