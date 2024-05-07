import type { TurboModule } from 'react-native/Libraries/TurboModule/RCTExport';
import { TurboModuleRegistry } from 'react-native';
import type { FeatureFlag, NewsFeedCard, ContentCard } from './index';

export interface Spec extends TurboModule {
  onUserLogin(profile: string): void;
  setDebugLevel(level: number): void;
  profileSet(profile: string): void;
  recordChargedEvent(
    details: string,
    items: string
  ): void;

}

export default TurboModuleRegistry.getEnforcing<Spec>('CleverTapReact');