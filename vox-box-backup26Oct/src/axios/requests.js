import axios from "axios";
import apiHost from "../apiHost";
export const ErrUnknown = new Error("Unknown")

const rTCHandshake = axios.create({
    baseURL:apiHost,
    headers:{
        Accept: `application/json`,
    },
})

const testing = axios.create({
    baseURL: "http://jsonplaceholder.typicode.com/posts",
    headers:{
        Accept: `application/json`,
    },
})
export const testGoogle = () => {
    const path = ""
    return testing.get(path)
}

export const sendOffer = (offer) => {
    // const path = "" 
    const path = `/offer`
    return rTCHandshake.post(path, offer)
}

// const initRooms = (isProd) => {
//     const server = isProd ? PROD_SERVER : STAGING_SERVER
//     const basePath = server + "/connect_api/v1"
//     return axios.create({
//         baseURL: basePath,
//         headers:{
//             Accept: `application/json`,
//         },
//     })

// }

// export const getAudioList = (popmapId, primaryStreamId) => {
//     let path = `/wave/light_audio_by_popmap?pop_map_id=${popmapId}`
//     const promise = fetchAudios.get(path) 
//     const dataPromise = promise.then(response => {
//         return response.data.langauges.map(lang => {
//                 const language = getLanguageForCode(lang)
//                 return {
//                     id: primaryStreamId,
//                     name: language.name,
//                     image: language.flagImage,
//                     lang_id: lang 
//                 }
//             }) }
//     )
//     return dataPromise
// }

// export const fetchTranslatorRooms = (streamId, isProd) => {
    
//     const data = {
//         "room_id": streamId
//     }

//     let path = "/listener_room_translations" 
//     if (rooms===null){
//         rooms = initRooms(isProd)
//     }
//     const promise = rooms.post(path, data)
//     const dataPromise = promise.then(response=> response).catch(
//         error => {
//             const status = error.response.status
//             if (status !== 'undefined' && status === 500) {
//                 console.log(error.response.status)
//                 throw ErrNoTranslation 
//             } else {
//                 throw ErrUnknown 
//             }
//         }
//     )

//     return dataPromise
// }