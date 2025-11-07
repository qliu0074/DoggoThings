import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '1m',
};

export default function () {
  const payload = JSON.stringify({
    userId: 1,
    items: [{ productId: 1, qty: 1 }],
    freezeBalance: false,
    address: 'Perf Street 1',
    phone: '13800000000',
  });

  const res = http.post('http://localhost:8080/api/v1/client/orders', payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
  });
  sleep(1);
}
