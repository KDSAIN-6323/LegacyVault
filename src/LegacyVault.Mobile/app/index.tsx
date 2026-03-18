import { Redirect } from 'expo-router';
import { useSelector } from 'react-redux';
import { RootState } from '../src/store';

export default function Index() {
  const user = useSelector((s: RootState) => s.auth.user);
  return <Redirect href={user ? '/app/' : '/auth/login'} />;
}
