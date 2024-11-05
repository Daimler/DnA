import React, { useState, useEffect,useRef } from 'react'; 
import classNames from 'classnames';
import Styles from './CodeSpaceTutorials.scss';
import VideoJS from './VideoJS'
import { Envs } from '../../Utility/envs';


const CodeSpaceTutorials = () => {
  const codespaceTutorials = [
    {
      id: '1',
      title: "Introduction to Codespaces",
      url:'code-space-tutorials/Intoduction.mp4',
      description: "An overview of what codespaces are and how they can be used."
    },
    {
      id: '2',
      title: "How to create a new Codespace",
      url: "code-space-tutorials/Creation of codespaces.mp4",
      description: "Step-by-step guide on creating a new codespace."
    },
    {
      id: '3',
      title: "How to deploy and monitor a Codespace",
      url: "code-space-tutorials/Deployment and View Log.mp4",
      description: "Deploy the code in stagging or production environment and monitoring the build & deployed application logs."
    },
    {
      id: '4',
      title: "How to manage secrets in a Codespace",
      url: "code-space-tutorials/EnvironmentVariableConfig.mp4",
      description: "Configure secrets for your deployed applications using environment variables "
    },
    {
      id: '5',
      title: "How to configure security in a Codespace",
      url: "code-space-tutorials/securityConfig.mp4",
      description: "Security configuration in codespaces using IAM authentication and Alice authorization"
    },
    {
      id: '6',
      title: "E.g. Codespace that uses python fastAPI",
      url: "code-space-tutorials/exampleCodespace-Python.mp4",
      description: "An example codespace that uses python fastAPI"
    },
    {
      id: '7',
      title: "Collaboration in Codespaces",
      url: "code-space-tutorials/Codespace Collaboration.mp4",
      description: "Steps for a collaborator to onboard into a Codespace"
    },
    {
      id: '8',
      title: "How to create a private recipe",
      url: "code-space-tutorials/Private Visibility Recipe Creation.mp4",
      description: "Steps to create a private recipe "
    },
    {
      id: '9',
      title: "How to create a public recipe",
      url: "code-space-tutorials/Public Visibility Recipe Creation.mp4",
      description: "Steps to create a public recipe"
    },
    {
      id: '10',
      title: "Create your own private gpt with Codespace",
      url: "code-space-tutorials/Create your own private gpt with codespace.mp4",
      description: "Steps to create your own private gpt with Codespace"
    },
  ];

  const [selectedVideo, setSelectedVideo] = useState(codespaceTutorials[0]);
  const [videoJsOptions, setVideoJsOptions] = useState(
    {
      autoplay: true,
      controls: true,
      responsive: true,
      fluid: true,
      sources: [{
        src: Envs.CODESPACE_TUTORIALS_BASE_URL + codespaceTutorials[0].url,
        type: 'video/mp4'
      }]
    }
  );
  
  useEffect(()=>{
    setVideoJsOptions({
      autoplay: true,
      controls: true,
      responsive: true,
      fluid: true,
      sources: [{
        src: Envs.CODESPACE_TUTORIALS_BASE_URL + selectedVideo.url,
        type: 'video/mp4'
      }]
    })

  },[selectedVideo])


  const playerRef = useRef(null);


  const handlePlayerReady = (player) => {
    playerRef.current = player;
    player.on('waiting', () => {
      console.log('player is waiting');
    });

    player.on('dispose', () => {
      console.log('player will dispose');
    });
  };


  return (
    <div className={classNames(Styles.wrapper)}>
      <h5 className={classNames(Styles.Modeltitle)}>Code Space Tutorials</h5>
      <div className={classNames(Styles.codeSpaceTutorials)}>
        <div className={classNames(Styles.leftPlane)}>
          <VideoJS options={videoJsOptions} onReady={handlePlayerReady} />
          <h5 className={classNames(Styles.selectedTitle)}>{selectedVideo.title}</h5>
          <span className={classNames(Styles.selectedDes)}>{selectedVideo.description}</span>
        </div>
        <div className={classNames(Styles.rightPlane)}>
          <table className={classNames('ul-table', Styles.tutorialsTable)}>
            <tbody>
              {codespaceTutorials?.map((item) => (
                <tr id={item.id} key={item.id} className={classNames('data-row', Styles.tutorialsRow, item.id === selectedVideo.id ? Styles.selectedRow : '')}>
                  <td className={'wrap-text'}>
                    <div className={Styles.tutorialtitle} onClick={() => { setSelectedVideo(item) }}><i className={classNames('icon mbc-icon sort' ,Styles.videoIcon)}/><span className={Styles.title} >{item.title}</span></div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default CodeSpaceTutorials;
