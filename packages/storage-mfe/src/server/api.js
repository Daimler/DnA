import axios from 'axios';
import { SESSION_STORAGE_KEYS } from '../components/Utility/constants';
import { Envs } from '../components/Utility/envs';
import { refreshToken } from 'dna-container/RefreshToken';

const jwt = sessionStorage?.length ? sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT) : null;

export const baseURL = Envs.STORAGE_API_BASEURL
  ? Envs.STORAGE_API_BASEURL
  : `http://${window.location.hostname}:7175/api`;

const headers = {
  Accept: 'application/json',
  'Content-Type': 'application/json',
  Authorization: jwt,
};

export const server = axios.create({
  baseURL,
  headers,
});

export const hostServer = axios.create({
  baseURL: Envs.API_BASEURL ? Envs.API_BASEURL : `http://${window.location.hostname}:7171/api`,
  headers,
});

export const trinoServer = axios.create({
  baseURL: Envs.TRINO_API_BASEURL ? Envs.TRINO_API_BASEURL : `http://${window.location.hostname}:7575/api`,
  headers,
});


async function blobToJson(blob) {
  const text = await blob.text();
  try {
    return JSON.parse(text);
  } catch (e) {
    return text;
  }
}

function createRefreshInterceptor(instance) {
  // Interceptor to handle token refresh on 403 error
  instance.interceptors.response.use(
    (response) => response,
    async (error) => {
      // check if the response exists and it is a Blob
      if (error.response && error.response.data instanceof Blob) {
        // convert the Blob to JSON
        error.response.data = await blobToJson(error.response.data);
      }
      console.log('error', error);
      if (error?.response && error?.response.status === 403 && error?.response?.data?.error_description?.includes("JWT is expired")) {
        // Refresh the token and retry the failed request.
        const jwt = sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT);
        const newJwt = await refreshToken(jwt);

        console.log("newJwt storage", newJwt);

        // Update the Authorization header in Axios instances.
        server.defaults.headers.Authorization = newJwt;
        hostServer.defaults.headers.Authorization = newJwt;
        trinoServer.defaults.headers.Authorization = newJwt;

        // Retry the original request with the new token.
        error.config.headers.Authorization = sessionStorage.getItem(SESSION_STORAGE_KEYS.JWT);
        return await axios.request(error.config);
      }
      return Promise.reject(error);
    }
  );
}

// Apply interceptor to server
createRefreshInterceptor(server);

// Apply interceptor to hostServer
createRefreshInterceptor(hostServer);

// Apply interceptor to trinoServer
createRefreshInterceptor(trinoServer);
