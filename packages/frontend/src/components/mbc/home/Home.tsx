import classNames from 'classnames';
import React from 'react';
import { IUserInfo } from 'globals/types';
import Styles from './Home.scss';
import { Envs } from 'globals/Envs';
import DNACard from 'components/card/Card';

export interface ILandingpageProps {
  user: IUserInfo;
}

const Home: React.FC<ILandingpageProps> = () => {
  return (
    <div className={Styles.dnaContainer}>
      <div className={classNames(Styles.dnaRow, Styles.moreSpaceRow)}>
        <div className={classNames(Styles.dnaCol6, Styles.colDivider)}>
          <div className={Styles.bannerDesc}>
            <h2 className={Styles.appName}>{Envs.DNA_APPNAME_HOME}</h2>
            <h5 className={Styles.appDesc}>
              Self services, data insight and
              <br />
              analytics - all in one place.
            </h5>
            <p className={Styles.poweredBy}>powered by CARLA</p>
          </div>
          <div className={classNames(Styles.dnaRow, Styles.transparencyCol)}>
            <div className={classNames(Styles.dnaCol6, Styles.transparencyCol)}>
              &nbsp;
            </div>
            <div className={classNames(Styles.dnaCol6, Styles.transparencyCol)}>
              <DNACard
                title={'Transparency'}
                description={'Explore all Data & AI Solutions in MB and view your Portfolio.'}
                url={'/transparency'}
                isTextAlignLeft={false}
                isDisabled={false}
                svgIcon={'transparency'}
                className="transparency"
                upperTag="MB"
              />
            </div>
          </div>
        </div>
        <div className={classNames(Styles.dnaCol6, Styles.colDivider)}>
          <div className={classNames(Styles.dnaRow, Styles.fcCol)}>
            <div className={Styles.dnaCol6}>
              <DNACard
                title={'CarLA'}
                description={'More Infos about the Cars Analysis Reporting Landscape'}
                url={'/data'}
                isTextAlignLeft={false}
                isDisabled={false}
                svgIcon={'data'}
                className="transparency"
              />
            </div>
            <div className={Styles.dnaCol6}>
              <DNACard
                title={'Data'}
                description={'From Data Products to Data Governance - all you need to work is here'}
                url={'/data'}
                isTextAlignLeft={false}
                isDisabled={false}
                svgIcon={'data'}
                className="data"
                upperTag="FC"
              />
            </div>
            <div className={Styles.dnaCol6}>
              <DNACard
                title={'Tools'}
                description={'Our standard Data & Analytics for both FC Users and Pro Developers'}
                url={'/tools'}
                isTextAlignLeft={false}
                isDisabled={false}
                svgIcon={'tools'}
                className="tools"
                upperTag="FC"
              />
            </div>
            <div className={Styles.dnaCol6}>
              <DNACard
                title={'Trainings'}
                description={'Data and Tools are not enough - here we enable you to become even more productive'}
                url={'/trainings'}
                isTextAlignLeft={false}
                isDisabled={!Envs.ENABLE_TRAININGS}
                svgIcon={'trainings'}
                className="trainings"
                upperTag="FC"
              />
            </div>
          </div>
        </div>
      </div>
      {/* <div className={Styles.dnaRow}>
        <div className={Styles.newsSection}>
          <div className={Styles.newsContainer}>
            <div className={Styles.newsItem}>
              <h3>V1.1 coming soon!</h3>
              <p>
                Follow our social intranet page to see pro...{' '}
                <a href="#" target="_blank" rel="noreferrer noopener">
                  read more
                </a>
              </p>
            </div>
          </div>
          <div className={Styles.newsContainer}>
            <div className={Styles.newsItem}>
              <h3>News Lorem</h3>
              <p>
                Follow our social intranet page to see pro...{' '}
                <a href="#" target="_blank" rel="noreferrer noopener">
                  read more
                </a>
              </p>
            </div>
          </div>
          <div className={Styles.newsContainer}>
            <div className={Styles.newsItem}>
              <h3>News Lorem</h3>
              <p>
                Follow our social intranet page to see pro...{' '}
                <a href="#" target="_blank" rel="noreferrer noopener">
                  read more
                </a>
              </p>
            </div>
          </div>
        </div>
      </div> */}
    </div>
  );
};

export default Home;
