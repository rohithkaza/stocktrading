import { initializeApp } from 'firebase/app';
import { getAuth, signInWithEmailAndPassword, GoogleAuthProvider, signInWithPopup, createUserWithEmailAndPassword } from 'firebase/auth';

// firebase setting
const firebaseConfig = {
  apiKey: "AIzaSyDfbloibrYvLdHBVP-oG8aKWvlPf1O-Ttg",
  authDomain: "rice-comp-539-spring-2022.firebaseapp.com",
  projectId: "rice-comp-539-spring-2022",
  storageBucket: "rice-comp-539-spring-2022.firebasestorage.app",
  messagingSenderId: "904917579142",
  appId: "1:904917579142:web:01c91764909192e6502807",
  measurementId: "G-SVJ90SYNPS"
};

// initialize firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const googleProvider = new GoogleAuthProvider();

export { auth, signInWithEmailAndPassword, googleProvider, signInWithPopup, createUserWithEmailAndPassword };
