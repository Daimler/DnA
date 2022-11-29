import { server } from '../server/api';

const getAllDataProducts = (sortBy, sortOrder) => {
  return server.get(`/dataproducts?limit=0&offset=0&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
    data: {},
  });
};

const getDataProductById = (id) => {
  return server.get(`/dataproducts/${id}`, {
    data: {},
  });
};

const getDepartments = () => {
  return server.get('/departments', {
    data: {},
  });
};

const createDataProduct = (data) => {
  return server.post('/dataproducts/provider', {
    data,
  });
};

const updateProvider = (data) => {
  return server.put('/dataproducts/provider', {
    data,
  });
};

const updateConsumer = (data) => {
  return server.put('/dataproducts/consume', {
    data,
  });
};

const deleteDataProduct = (id) => {
  return server.delete(`/dataproducts/${id}`, {
    data: {},
  });
};

const getDataComplianceList = (offset, limit, sortBy, sortOrder) => {
  return server.get(`datacompliance?offset=${offset}&limit=${limit}&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
    data: {},
  });
};

const getAllClassificationTypes = () => {
  return server.get('/classifications', { data: {} });
};

const getAllLegalBasis = () => {
  return server.get('/legalbasis', { data: {} });
};

const getPlanningIT = (searchTerm = '') => {
  return server.get(`/planningit?searchTerm=${searchTerm}`, { data: {} });
};

export const dataTransferApi = {
  getAllDataProducts,
  createDataProduct,
  getDepartments,
  updateProvider,
  updateConsumer,
  deleteDataProduct,
  getDataProductById,
  getDataComplianceList,
  getAllClassificationTypes,
  getAllLegalBasis,
  getPlanningIT,
};
