import { hostServer, server } from '../server/api';

const getAllDataProductList = (sortBy, sortOrder) => {
  return server.get(`/dataproducts?limit=0&offset=0&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
    data: {},
  });
};

const getDataProductById = (id) => {
  return server.get(`/dataproducts/${id}`, {
    data: {},
  });
};

const createDataProduct = (data) => {
  return server.post('/dataproducts', {
    data,
  });
};

const updateDataProduct = (data) => {
  return server.put('/dataproducts', {
    data,
  });
};

const deleteDataProduct = (id) => {
  return server.delete(`/dataproducts/${id}`, {
    data: {},
  });
};

const createDataTransfer = (id, data) => {
  return server.post(`/dataproducts/${id}/datatransfer`, {
    data,
  });
};

const getAllDataTransfers = (dataTransferIds, sortBy, sortOrder) => {
  return server.get(
    `/datatransfers?limit=0&offset=0&datatransferIds=${dataTransferIds}&sortBy=${sortBy}&sortOrder=${sortOrder}`,
    {
      data: {},
    },
  );
};

const getMyDataTransfers = (dataTransferIds, sortBy, sortOrder) => {
  return server.get(
    `/datatransfers?limit=0&offset=0&isCreator=true&datatransferIds=${dataTransferIds}&sortBy=${sortBy}&sortOrder=${sortOrder}`,
    {
      data: {},
    },
  );
};

const getAllAgileReleaseTrains = () => {
  return server.get('/agileReleaseTrains?limit=0&offset=0', { data: {} });
};

const getAllCarlaFunctions = () => {
  return server.get('/carlafunctions?limit=0&offset=0', { data: {} });
};

const getAllCorporateDataCatalogs = () => {
  return hostServer.get('/datasources?source=CDC', { data: {} });
};

const getAllPlatforms = () => {
  return server.get('/platforms?limit=0&offset=0', { data: {} });
};

const getAllFrontEndTools = () => {
  return server.get('/frontEndTools?limit=0&offset=0', { data: {} });
};

export const dataProductApi = {
  getAllDataProductList,
  getDataProductById,
  createDataProduct,
  updateDataProduct,
  deleteDataProduct,
  createDataTransfer,
  getAllDataTransfers,
  getMyDataTransfers,
  getAllAgileReleaseTrains,
  getAllCarlaFunctions,
  getAllCorporateDataCatalogs,
  getAllPlatforms,
  getAllFrontEndTools,
};
