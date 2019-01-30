import {fetchMonitor} from '../service/api';

export default {
  namespace: 'monitor',
  state: {
    appName:'',
    cpu:[],
    memory:[],
    containers: [],
    applicationId: '',
  },

  effects: {

    *queryMonitorInfo({ payload }, { call, put }) {
      const response = yield call(fetchMonitor, payload);
      yield put({
        type: 'queryMonitorInfoRet',
        payload: response,
      });
    },

  },

  reducers: {
    queryMonitorInfoRet(state, { payload }) {
      const data = payload.data;
      const cpu = [];
      const memory = [];
      for(let i=0;i<data.length; i++) {
        const each = data[i];
        cpu.push({value: [each[4],each[2]]});
        memory.push({value: [each[4], each[3]]});
      }

      // containers
      const containers = [];
      for(let i=0;i<payload.containers.length; i++){
        const each = payload.containers[i];
        containers.push({
          'containerId': each[0],
          'hostName': each[1],
          'role': each[3],
        })
      }

      // appInfo
      const applicationId = payload.appInfo.applicationID;

      return {
        ...state,
        cpu: cpu,
        memory: memory,
        containers: containers,
        applicationId: applicationId,
      };
    },

  },
};
