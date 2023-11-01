import { useEffect, useRef, useState } from "react";
import { sendOffer, testGoogle } from "./axios/requests";
import adapter from 'webrtc-adapter';
import "./Player.css"
import logo from "./images/vox_logo.svg";
import { RotatingLines } from "react-loader-spinner";
import { useHistory } from "react-router-dom/cjs/react-router-dom.min";
const JoinStream = (props) => {
  const history = useHistory()

  const playStream = () => {
    history.push("/stream")
  }
  return (
    <>
      <div className="Player">
        <header className="App-header">
          <div className="logo-presenter-block">
            <img src={logo} alt="logo" className="App-logo" />
          </div>
          <div className="player-main-body">
          </div>
          <div className="player-footer">
            <div className="joinstream">
              {
                <button onClick={playStream} className="btn-primary" id="start_play_button">JOIN STREAM</button>
              }
            </div>
          </div>
        </header>
      </div>
    </>
  )
}

export default JoinStream;
