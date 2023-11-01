import { useEffect, useRef, useState } from "react";
import { sendOffer, testGoogle } from "./axios/requests";
import adapter from 'webrtc-adapter';
import "./Player.css"
import logo from "./images/vox_logo.svg";
import { RotatingLines } from "react-loader-spinner";
import { useHistory } from "react-router-dom/cjs/react-router-dom.min";
const PlayStream = (props) => {
  const history = useHistory()
  const localAudioRef = useRef()
  const remoteAudioRef = useRef()
  const iceCandidatesRef = useRef(new Array())
  const pc = useRef(new RTCPeerConnection(null))
  const textAreaRef = useRef()
  const responseTextAreaRef = useRef()

  const [isLoading, setIsLoading] = useState(false)
  const [isActiveStream, setIsActiveStream] = useState(false)
  const [ready, setReady] = useState(false)

  const spinner = () => {
    console.log("Loading Spinner");
    return (
      <header className="App-header">
        <RotatingLines
          strokeColor="grey"
          strokeWidth="5"
          animationDuration="0.75"
          width="96"
          visible={true}
        />
      </header>
    );
  };


  useEffect(() => {

    const _pc = new RTCPeerConnection(null)
    _pc.onicecandidate = (e) => {
      if (e.candidate) {
        console.log("icecandidates here:" + JSON.stringify(e.candidate))
        iceCandidatesRef.current.push(e.candidate)
      }
    }
    _pc.oniceconnectionstatechange = (e) => {
      // helps check the state of the peer connection
      // possible values are connected, disconnected, failed, close
      console.log(e)
    }
    _pc.ontrack = (e) => {
      // we got remote stream
      remoteAudioRef.current.srcObject = e.streams[0]
      setIsLoading(false)
      setIsActiveStream(true)
    }

    pc.current = _pc

    setReady(true)
    return () => {
      // cleanup
    }
  }, [])

  useEffect(() => {
    if (ready === true) {
      rTChandshake()
    }
  }, [ready])

  const stopStream = () => {
    history.push("/")
  }
  const createOffer = () => {
    pc.current.createOffer({
      offerToReceiveVideo: 0, // optional .. not needed to give
      offerToReceiveAudio: 1, // optional .. not needed to give
    }).then(sdp => {
      console.log(JSON.stringify(sdp))
      pc.current.setLocalDescription(sdp)
      // textAreaRef.current.value = JSON.stringify(sdp)
    }).catch(e => console.log(e))
  }


  const rTChandshake = () => {
    setIsLoading(true)
    pc.current.createOffer({
      offerToReceiveVideo: 0, // optional .. not needed to give
      offerToReceiveAudio: 1, // optional .. not needed to give
    }).then(sdp => {
      console.log(JSON.stringify(sdp))
      pc.current.setLocalDescription(sdp)
      // textAreaRef.current.value = JSON.stringify(sdp)
      return new Promise(resolve => setTimeout(() => resolve(sdp), 3000))
    }).then(sdpData => {
      const offerSdpAndCandidates = {
        icecandidates: iceCandidatesRef.current,
        sdp: sdpData
      }
      console.log("offerSdpAndCandidates:" + JSON.stringify(offerSdpAndCandidates))

      return sendOffer(offerSdpAndCandidates)
    }).then(data => {
      console.log(JSON.stringify(data))
      // pc.current.setLocalDescription(sdp) // set remote description here
      // responseTextAreaRef.current.value = JSON.stringify(data)

      return handleAnswer(data)
    }).catch(e => console.log(e))
  }

  const handleAnswer = async (data) => {
    await pc.current.setRemoteDescription(new RTCSessionDescription(data.data.sdp))

    for (const candidate in data.data.icecandidates) {
      console.log("Adding Candidates");
      console.log(data.data.icecandidates[candidate])
      await pc.current.addIceCandidate(new RTCIceCandidate(data.data.icecandidates[candidate]));
    }
  }

  const createAnswer = () => {
    pc.current.createAnswer({
      offerToReceiveVideo: 1, // optional .. not needed to give
      offerToReceiveAudio: 1, // optional .. not needed to give
    }).then(sdp => {
      console.log(JSON.stringify(sdp))
      pc.current.setLocalDescription(sdp)
    }).catch(e => console.log(e))
  }

  const setRemoteDescription = () => {
    const sdp = JSON.parse(textAreaRef.current.value)
    console.log(sdp)
    pc.current.setRemoteDescription(new RTCSessionDescription(sdp))
  }

  const addCandidate = () => {
    const candidate = JSON.parse(textAreaRef.current.value)
    console.log('Adding candidate...', candidate)
    pc.current.addIceCandidate(new RTCIceCandidate(candidate))
  }

  // return (
  //   <div style={{ margin: 10 }}>
  //     <video style={{
  //       width: 240, height: 240,
  //       margin: 5, backgroundColor: "black"
  //     }}
  //       ref={localAudioRef} autoPlay></video>
  //     <video style={{
  //       width: 240, height: 240,
  //       margin: 5, backgroundColor: "black"
  //     }}
  //       ref={remoteAudioRef} autoPlay></video>
  //     <br />
  //     {/* <button onClick={createOffer}>Create Offer</button> */}
  //     <button onClick={rTChandshake}>Create Offer</button>
  //     <button onClick={createAnswer}>Create Answer</button>
  //     <br />
  //     <textarea ref={textAreaRef}></textarea>
  //     <br />
  //     <button onClick={setRemoteDescription}>Set Remote Description</button>
  //     <button onClick={addCandidate}>Add Candidates</button>
  //     <br />
  //     <textarea ref={responseTextAreaRef}></textarea>
  //     <br />
  //   </div>
  // );
  return (
    <>
      <div className="Player">
        <header className="App-header">
          <div className="logo-presenter-block">
            <img src={logo} alt="logo" className="App-logo" />
          </div>
          <div className="player-main-body">
            {isLoading === true ? spinner() : <div>Connected</div>}
            <video hidden autoPlay controls playsInline ref={remoteAudioRef}></video>
          </div>
          <div className="player-footer">
            <div className="joinstream">
              {isLoading === true ?<button disabled="true" className="btn-primary" id="start_play_button">JOIN STREAM</button>:
                <button onClick={stopStream} className="btn-primary" id="start_play_button">STOP STREAM</button>
              }
            </div>
          </div>
        </header>
      </div>
    </>
  )
}

export default PlayStream;

