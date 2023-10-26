import { useEffect, useRef } from "react";
import { sendOffer, testGoogle } from "./axios/requests";
import adapter from 'webrtc-adapter';
function App() {
  const localAudioRef = useRef()
  const remoteAudioRef = useRef()
  const iceCandidatesRef = useRef(new Array())
  const pc = useRef(new RTCPeerConnection(null))
  const textAreaRef = useRef()
  const responseTextAreaRef = useRef()

  useEffect(() => {

    /* getUserMedia start */
    const constraints = {
      audio: false,
      video: false,
    }


    // navigator.mediaDevices.getUserMedia(constraints)
    //   .then(stream => {
    //     // display audio/video
    //     localAudioRef.current.srcObject = stream
    //   })
    //   .catch(e => {
    //     console.log("error occured:", e)
    //   })


    /* getUserMedia end */

    const _pc = new RTCPeerConnection(null)

    navigator.mediaDevices.getUserMedia(constraints)
      .then(stream => {
        // display audio/video
        localAudioRef.current.srcObject = stream
        stream.getTracks().forEach(track => {
          _pc.addTrack(track, stream)
        })
      })
      .catch(e => {
        console.log("error occured:", e)
      })

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
    }

    pc.current = _pc

    return () => {
      // cleanup
    }
  }, [])

  const createOffer = () => {
    pc.current.createOffer({
      offerToReceiveVideo: 0, // optional .. not needed to give
      offerToReceiveAudio: 1, // optional .. not needed to give
    }).then(sdp => {
      console.log(JSON.stringify(sdp))
      pc.current.setLocalDescription(sdp)
      textAreaRef.current.value = JSON.stringify(sdp)
    }).catch(e => console.log(e))
  }

  const rTChandshake = () => {
    pc.current.createOffer({
      offerToReceiveVideo: 0, // optional .. not needed to give
      offerToReceiveAudio: 1, // optional .. not needed to give
    }).then(sdp => {
      console.log(JSON.stringify(sdp))
      pc.current.setLocalDescription(sdp)
      textAreaRef.current.value = JSON.stringify(sdp)
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
      responseTextAreaRef.current.value = JSON.stringify(data)

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

  return (
    <div style={{ margin: 10 }}>
      <video style={{
        width: 240, height: 240,
        margin: 5, backgroundColor: "black"
      }}
        ref={localAudioRef} autoPlay></video>
      <video style={{
        width: 240, height: 240,
        margin: 5, backgroundColor: "black"
      }}
        ref={remoteAudioRef} autoPlay></video>
      <br />
      {/* <button onClick={createOffer}>Create Offer</button> */}
      <button onClick={rTChandshake}>Create Offer</button>
      <button onClick={createAnswer}>Create Answer</button>
      <br />
      <textarea ref={textAreaRef}></textarea>
      <br />
      <button onClick={setRemoteDescription}>Set Remote Description</button>
      <button onClick={addCandidate}>Add Candidates</button>
      <br />
      <textarea ref={responseTextAreaRef}></textarea>
      <br />
    </div>
  );
}

export default App;
