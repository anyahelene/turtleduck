import path from 'path';
const __dirname = path.dirname(new URL(import.meta.url).pathname)
const __filename = new URL(import.meta.url).pathname
//const { CleanWebpackPlugin } = require('clean-webpack-plugin');
import * as marked from 'marked';
import webpack from 'webpack';
//import HotModuleReplacementPlugin from 'webpack/lib/HotModuleReplacementPlugin';
const renderer = new marked.Renderer();
const hmrp = new webpack.HotModuleReplacementPlugin();
const isDevServer = process.env.WEBPACK_SERVE;

export default {
  mode: 'development',
  devtool: 'inline-source-map',
  context: path.resolve(__dirname, 'src', 'main', 'webroot'),
  //plugins: [ new CleanWebpackPlugin() ],
  stats: {
    loggingDebug: ["sass-loader"],
  },
  entry: {
    bundle: { import: ['./index.js', './css/style.scss', './css/common.scss', './css/buttons.scss', './css/frames.scss', './css/markdown.scss', './terms-no.md'], filename: 'js/bundle.js' },
    // path.join(__dirname, 'src', 'main', 'webroot','css', 'style.scss'),
    //html: ['./webroot/terms-no.md'],
  },
  output: {
    path: path.resolve(__dirname, 'target', 'classes', 'webroot'),
    //    publicPath: 'static/',
    filename: 'js/[name].[contenthash].js'
  },
  devServer: {
      static: ['./target/classes/webroot/',
          {
            directory: '../fonts/',
            publicPath: '/fonts'
          },
          { 
            directory: '../py/pyodide',
            publicPath: '/py'
          }
      ],
      hot: 'only',
      liveReload: false,
      watchFiles: ['../borb/**/*.ts', '../borb/**/*.js']
  },
  resolve: {
      extensions: [".ts", ".mts", ".tsx", ".js", ".mjs"],
      symlinks: false,
      alias: {
//          'borb$': path.resolve(__dirname, 'src/main/webroot/borb/borb'),
  //        'borb': path.resolve(__dirname, 'src/main/webroot/borb'),
  //        '../../../../borb/src/Styles.ts$': path.resolve(__dirname, '../borb/src/Styles.ts'),
    //      '../../../../borb/src': path.resolve(__dirname, '../borb/src')
      }
  //  fallback: { 
  //      "querystring": require.resolve("querystring-es3/"),
  //      "buffer": require.resolve("buffer/")
  //  }
  },
  optimization: {
    usedExports: true
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
  cache: {
    type: 'filesystem',
    cacheDirectory: path.resolve(__dirname, `node_modules/.cache/webpack${isDevServer?'-serve':''}`),
    buildDependencies: { config: [__filename, path.resolve(__dirname, 'tsconfig.json')] },
  },
  module: {
    rules: [
      { test: /\.m?tsx?$/, loader : "ts-loader" },
      {
        test: /\.html$/,
        //exclude: /node_modules/,
        use: [
          { loader: 'file-loader', options: { name: '[name].[ext]', publicPath: '' } },
          { loader: 'extract-loader', options: {} },
          {
            loader: 'html-loader',
            options: {
              sources: {
                list: [
                  { tag: 'img', attribute: 'src', type: 'src' },
                  { tag: 'link', attribute: 'href', type: 'src' },
                  //  { tag: 'script', attribute: 'src', type: 'src' },
                ]
              },
            }
          }
        ]
      },
      {
          resourceQuery: /raw/,
          type: 'asset/source'
      },
      {
        test: /\.md$/,
        //exclude: /node_modules/,
        use: [
          { loader: 'file-loader', options: { name: '[name].html', publicPath: '' } },
          { loader: 'extract-loader', options: {} },
          {
            loader: 'html-loader',
            options: {
              sources: {
                list: [
                  { tag: 'img', attribute: 'src', type: 'src' },
                  { tag: 'link', attribute: 'href', type: 'src' },
                  //  { tag: 'script', attribute: 'src', type: 'src' },
                ]
              },
            }
          },
          {
            loader: "markdown-loader",
            options: {
              pedantic: false,
              renderer: renderer
            }
          }
        ]
      }, {
        test: /\.css$/,
        type: 'asset/resource',
        generator: {
            filename: '[name].[ext]'
        },
        //exclude: /node_modules/,
        use: [
//            { loader: 'raw-loader' },
//            { loader: 'file-loader', options: { name: '[name].[ext]' } },
            'extract-loader',
            { loader: 'css-loader', options: { importLoaders: 1 } },
        ]
      },
      {
        test: /\.txt$/,
        use: [{ loader: 'raw-loader' }]
      },
      {
        test: /\.s[ac]ss$/i,
        resourceQuery: { not: [/raw/] },
        type: 'asset/resource',
        generator: {
            filename: '[path][name].css'
        },
        exclude: /node_modules/,
        use: [
        //  {
        //    loader:
        //      'file-loader', options: { name: 'css/[name].css', publicPath: 'css' }
        //  },
            "extract-loader",
          // Translates CSS into CommonJS
          { loader: "css-loader", options: { url: false } },
          // Compiles Sass to CSS
          //"sass-loader",
          {
            loader: "sass-loader", options: {
              webpackImporter: true,
              //    sassOptions: {includePaths: [path.resolve(__dirname, 'node_modules'),]}
            }
          }
        ]
      },
      {

        test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
        //exclude: /fonts/,
        loader: 'url-loader',
        options: {
          limit: 200000, outputPath: 'static',
        }
      }
    ]
  },
};
