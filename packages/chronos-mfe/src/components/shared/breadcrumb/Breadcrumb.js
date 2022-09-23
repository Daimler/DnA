import React from 'react';
import Styles from './Breadcrumb.scss';

const Breadcrumb = (props) => {
  return (
    <div className={Styles.breadcrumb}>
      <ol>
          <li><a href='#/'>Start</a></li>
          <li><a href='#/services'>My Services</a></li>
          { props.children }
      </ol>
    </div>
  )
}

export default Breadcrumb;