import { create } from 'zustand';

export const useLoadingStore = create((set) => ({
  loading: false,
  setLoading: (v) => set({ loading: v }),
}));

// export const useLoadingStore = loadingStore;