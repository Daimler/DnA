import { server } from '../server/api';

const getAllDataProducts = () => {
  return server.get('/dataproducts?limit=0&offset=0', {
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

export const dataProductsApi = {
  getAllDataProducts,
  createDataProduct,
  updateDataProduct,
  deleteDataProduct,
  getDataProductById,
};
