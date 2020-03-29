var path = require('path');

module.exports = {
  mode: 'development',
  context: path.resolve(__dirname, 'src'),
  entry: [ path.join(__dirname, 'src', 'main', 'webapp', 'index.js'),
//	   path.join(__dirname, 'src', 'main', 'webapp', 'style.css'),
//	   path.join(__dirname, 'src', 'main', 'webapp', 'index.html')
  ],
  output: {
    path: path.resolve(__dirname, 'target', 'generated', 'js'),
    filename: 'app.js'
  },
  module: {
    rules: [
      {
        test: /\.html$/,
	exclude: /node_modules/,
        use: [ { loader: 'file-loader', options: { name: '[name].[ext]' } },
		'extract-loader', 
		{ loader: 'html-loader', options: { } } ]
      },
      {
        test: /\.css$/,
	exclude: /node_modules/,
        use: [ { loader: 'file-loader', options: { name: '[name].[ext]' } },
		'extract-loader', 
		{ loader: 'css-loader', options: { importLoaders: 1 } },
		'postcss-loader']
      },
      {
        test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
        loader: 'url-loader',
        options: {
          limit: 10000
        }
      }
    ]
  },
};

