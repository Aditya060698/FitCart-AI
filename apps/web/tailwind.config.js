/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#10212e",
        mist: "#eef4ef",
        pine: "#1f6b57",
        leaf: "#52a37c",
        sand: "#f8f4ea",
        ember: "#b8613a",
      },
      boxShadow: {
        panel: "0 24px 80px rgba(16, 33, 46, 0.10)",
      },
      backgroundImage: {
        "fitcart-glow":
          "radial-gradient(circle at top left, rgba(82, 163, 124, 0.28), transparent 34%), radial-gradient(circle at right, rgba(184, 97, 58, 0.16), transparent 26%)",
      },
    },
  },
  plugins: [],
};
