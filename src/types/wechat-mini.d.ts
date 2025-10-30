// English comment: Minimal global typings for WeChat Mini Program used by custom tabBar.
// Keep this file WITHOUT imports/exports so that it contributes global names.

// Current native pages stack. Last item is the current page.
// `route` is like "pages/home/index" (without leading "/").
declare function getCurrentPages(): Array<{ route?: string }>

// Small subset of wx.* we need.
declare const wx: {
  /** English: Switch to a tab page defined in app.config tabBar.list */
  switchTab(options: { url: string }): void
}

// Event type for tap with dataset access.
declare interface TapEvent {
  currentTarget: {
    dataset: Record<string, unknown>
  }
}

// Instance shape for `this` inside Component lifetimes/methods.
declare interface MiniComponentInstance<TData = Record<string, unknown>> {
  data: TData
  setData(patch: Partial<TData>): void
}

// Component options with typed `this`.
declare interface MiniComponentOptions<TData, TMethods> {
  data?: TData
  lifetimes?: {
    attached?(this: MiniComponentInstance<TData>): void
  }
  pageLifetimes?: {
    show?(this: MiniComponentInstance<TData>): void
  }
  methods?: TMethods & ThisType<MiniComponentInstance<TData> & TMethods>
}

// Global Component constructor
declare function Component<TData, TMethods>(
  options: MiniComponentOptions<TData, TMethods>
): void
