import { stringify } from 'qs';
import request from '@/utils/request';

export async function stopService() {
  return request('/stop');
}

export async function fetchMonitor(param) {
  const url = param.currentContainer?'/monitor?currentContainer='+ param.currentContainer : '/monitor';
  return request(url);
}









