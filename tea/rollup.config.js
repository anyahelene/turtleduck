import resolve from '@rollup/plugin-node-resolve';
//import commonjs from '@rollup/plugin-commonjs';
//import replace from '@rollup/plugin-replace';
//import babel from '@rollup/plugin-babel';
//import eslint from '@rollup/plugin-eslint';


export default {
      input: 'src/main/webroot/editor.js',
      external: ['classes'],
      output: {
              file: 'target/classes/webroot/js/editor.js',
              format: 'iife',
              sourcemap: true,
              name: 'TurtleDuck',
              globals: {classes: 'main'}
            },
      plugins: [ resolve(
          //{
      //      nextjs: false,
       //     browser: true,
       //     module: false,
       //   }
             ),
//          commonjs(),
 //         babel({
 //           exclude: 'node_modules/**',
 //         }),
 //         replace({
 //           preventAssignment: true,
 //           //exclude: 'node_modules/**',
//            'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'development'),
//          }),
      ]
};
