const path = require('path');
//const { CleanWebpackPlugin } = require('clean-webpack-plugin');

module.exports = {
  mode: 'development',
  context: path.resolve(__dirname, 'src', 'main'),
  //plugins: [ new CleanWebpackPlugin() ],
  stats: 'normal',
  entry: { 
      bundle: { import: ['./webroot/index.js', './webroot/css/style.scss'], filename: 'js/bundle.js' },
	  // path.join(__dirname, 'src', 'main', 'webroot','css', 'style.scss'),
	  //html: ['./webroot/foobar.html', './webroot/index.html'],
  },
  output: {
    path: path.resolve(__dirname, 'target', 'classes', 'webroot'),
    publicPath: 'static/',
    filename: 'js/[name].[contenthash].js'
  },
  optimization: {
      //runtimeChunk: 'single',
      //splitChunks: {
      //    cacheGroups: {
      //        libs: {
      //            test: /[\\/]node_modules[\\/]/,
      //            name: 'libs',
      //            chunks: 'all',
      //        },
      //    },
      //},
 },
  module: {
    rules: [
      {
        test: /\.html$/,
	//exclude: /node_modules/,
        use: [ 
            { loader: 'file-loader', options: { name: '[name].[ext]', publicPath: '' } },
            { loader: 'extract-loader', options: {} }, 
		    {
                loader: 'html-loader',
                options: { 
                    attributes: { list: [
                       { tag: 'img', attribute: 'src', type: 'src' },
                       { tag: 'link', attribute: 'href', type: 'src' },
                      //  { tag: 'script', attribute: 'src', type: 'src' },
                    ] },
                }
            }
        ]
      },
      {
        test: /\.css$/,
	//exclude: /node_modules/,
        use: [ { loader: 'file-loader', options: { name: '[name].[ext]' } },
		'extract-loader', 
		{ loader: 'css-loader', options: { importLoaders: 1 } },
		]
      },
      {
          test: /\.s[ac]ss$/i,
	exclude: /node_modules/,
          use: [
              { loader: 'file-loader', options: { name: '[name].css' } },
              "extract-loader",
              // Translates CSS into CommonJS
              { loader: "css-loader", options: { url: true } },
              // Compiles Sass to CSS
              "sass-loader", // { loader: "sass-loader", options: { webpackImporter: true} }
          ]
      },
      {
        
        test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
        //exclude: /fonts/,
        loader: 'url-loader',
        options: {
            limit: 20000, outputPath: '',
        }
      }
    ]
  },
};

