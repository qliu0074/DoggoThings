// English comment: App root for Taro. It mounts current page as children.
import React, { type PropsWithChildren } from "react";
import "./app.scss";

/**
 * English comment: Accept only React children. No extra props.
 * Using PropsWithChildren avoids 'any' and keeps TS strict.
 */
export default function App({ children }: PropsWithChildren): React.ReactElement {
  // English comment: Render current page injected by Taro runtime.
  return <>{children}</>;
}
