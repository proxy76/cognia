// Configure webpack dev server to use port 8081
// so it doesn't conflict with the Ktor backend server on port 8080
config.devServer = {
    ...config.devServer,
    port: 8081,
};
