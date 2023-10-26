import React from 'react';
import ReactDOM from 'react-dom/client';
import { Route, BrowserRouter as Router, Switch } from "react-router-dom";
import './index.css';
import JoinStream from './JoinStream';
import PlayStream from './PlayStream';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <Router>
      <div>
        <Switch>
          <Route path="/stream" component={PlayStream} />
          <Route path="/" component={JoinStream} />
        </Switch>
      </div>
    </Router>
    {/* <App /> */}
  </React.StrictMode>
);

