import { stringify } from 'qs';
import request from '@/utils/request';

export async function stopService() {
  return request('/stop');
}

export async function fetchMonitor() {
  return request('/monitor');
}









