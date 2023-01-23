package turtleduck.gl.compat;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.NativeType;

import turtleduck.annotations.Nullable;

public interface ProgramInterfaceQuery extends Extension{

    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_BUFFER_VARIABLE = 0x92E5;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_COMPUTE_SUBROUTINE = 0x92ED;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_COMPUTE_SUBROUTINE_UNIFORM = 0x92F3;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_FRAGMENT_SUBROUTINE = 0x92EC;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_FRAGMENT_SUBROUTINE_UNIFORM = 0x92F2;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_GEOMETRY_SUBROUTINE = 0x92EB;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_GEOMETRY_SUBROUTINE_UNIFORM = 0x92F1;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_PROGRAM_INPUT = 0x92E3;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_PROGRAM_OUTPUT = 0x92E4;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_SHADER_STORAGE_BLOCK = 0x92E6;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_TESS_CONTROL_SUBROUTINE = 0x92E9;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_TESS_CONTROL_SUBROUTINE_UNIFORM = 0x92EF;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_TESS_EVALUATION_SUBROUTINE = 0x92EA;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_TESS_EVALUATION_SUBROUTINE_UNIFORM = 0x92F0;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_TRANSFORM_FEEDBACK_VARYING = 0x92F4;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_UNIFORM = 0x92E1;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_UNIFORM_BLOCK = 0x92E2;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_VERTEX_SUBROUTINE = 0x92E8;
    /**
     * Accepted by the {@code programInterface} parameter of GetProgramInterfaceiv,
     * GetProgramResourceIndex, GetProgramResourceName, GetProgramResourceiv,
     * GetProgramResourceLocation, and GetProgramResourceLocationIndex.
     */
    int GL_VERTEX_SUBROUTINE_UNIFORM = 0x92EE;
    /** Accepted by the {@code pname} parameter of GetProgramInterfaceiv. */
    int GL_ACTIVE_RESOURCES = 0x92F5;
    /** Accepted by the {@code pname} parameter of GetProgramInterfaceiv. */
    int GL_MAX_NAME_LENGTH = 0x92F6;
    /** Accepted by the {@code pname} parameter of GetProgramInterfaceiv. */
    int GL_MAX_NUM_ACTIVE_VARIABLES = 0x92F7;
    /** Accepted by the {@code pname} parameter of GetProgramInterfaceiv. */
    int GL_MAX_NUM_COMPATIBLE_SUBROUTINES = 0x92F8;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_ACTIVE_VARIABLES = 0x9305;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_ARRAY_SIZE = 0x92FB;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_ARRAY_STRIDE = 0x92FE;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_ATOMIC_COUNTER_BUFFER_INDEX = 0x9301;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_BLOCK_INDEX = 0x92FD;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_BUFFER_BINDING = 0x9302;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_BUFFER_DATA_SIZE = 0x9303;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_IS_PER_PATCH = 0x92E7;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_IS_ROW_MAJOR = 0x9300;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_LOCATION = 0x930E;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_LOCATION_INDEX = 0x930F;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_MATRIX_STRIDE = 0x92FF;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_NAME_LENGTH = 0x92F9;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_NUM_ACTIVE_VARIABLES = 0x9304;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_OFFSET = 0x92FC;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_REFERENCED_BY_COMPUTE_SHADER = 0x930B;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_REFERENCED_BY_FRAGMENT_SHADER = 0x930A;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_REFERENCED_BY_GEOMETRY_SHADER = 0x9309;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_REFERENCED_BY_TESS_CONTROL_SHADER = 0x9307;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_REFERENCED_BY_TESS_EVALUATION_SHADER = 0x9308;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_REFERENCED_BY_VERTEX_SHADER = 0x9306;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_TOP_LEVEL_ARRAY_SIZE = 0x930C;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_TOP_LEVEL_ARRAY_STRIDE = 0x930D;
    /** Accepted in the {@code props} array of GetProgramResourceiv. */
    int GL_TYPE = 0x92FA;

    /**
     * Queries a property of an interface in a program.
     *
     * @param program          the name of a program object whose interface to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} to query. One of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_UNIFORM UNIFORM}</td>
     *                         <td>{@link GL43C#GL_UNIFORM_BLOCK UNIFORM_BLOCK}</td>
     *                         <td>{@link GL43C#GL_PROGRAM_INPUT PROGRAM_INPUT}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         <td>{@link GL43C#GL_BUFFER_VARIABLE
     *                         BUFFER_VARIABLE}</td>
     *                         <td>{@link GL43C#GL_SHADER_STORAGE_BLOCK
     *                         SHADER_STORAGE_BLOCK}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE
     *                         VERTEX_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE
     *                         TESS_CONTROL_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE
     *                         TESS_EVALUATION_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE
     *                         GEOMETRY_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE
     *                         FRAGMENT_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE
     *                         COMPUTE_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE_UNIFORM
     *                         VERTEX_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE_UNIFORM
     *                         TESS_CONTROL_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE_UNIFORM
     *                         TESS_EVALUATION_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE_UNIFORM
     *                         GEOMETRY_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE_UNIFORM
     *                         FRAGMENT_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE_UNIFORM
     *                         COMPUTE_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_TRANSFORM_FEEDBACK_VARYING
     *                         TRANSFORM_FEEDBACK_VARYING}</td>
     *                         <td>{@link GL42#GL_ATOMIC_COUNTER_BUFFER
     *                         ATOMIC_COUNTER_BUFFER}</td>
     *                         </tr>
     *                         </table>
     * @param pname            the name of the parameter within
     *                         {@code programInterface} to query. One of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_ACTIVE_RESOURCES
     *                         ACTIVE_RESOURCES}</td>
     *                         <td>{@link GL43C#GL_MAX_NAME_LENGTH
     *                         MAX_NAME_LENGTH}</td>
     *                         <td>{@link GL43C#GL_MAX_NUM_ACTIVE_VARIABLES
     *                         MAX_NUM_ACTIVE_VARIABLES}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_MAX_NUM_COMPATIBLE_SUBROUTINES
     *                         MAX_NUM_COMPATIBLE_SUBROUTINES}</td>
     *                         </tr>
     *                         </table>
     * @param params           a variable to retrieve the value of {@code pname} for
     *                         the program interface
     */
    void glGetProgramInterfaceiv(int program, int programInterface, int pname, IntBuffer params);

    /**
     * Queries a property of an interface in a program.
     *
     * @param program          the name of a program object whose interface to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} to query. One of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_UNIFORM UNIFORM}</td>
     *                         <td>{@link GL43C#GL_UNIFORM_BLOCK UNIFORM_BLOCK}</td>
     *                         <td>{@link GL43C#GL_PROGRAM_INPUT PROGRAM_INPUT}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         <td>{@link GL43C#GL_BUFFER_VARIABLE
     *                         BUFFER_VARIABLE}</td>
     *                         <td>{@link GL43C#GL_SHADER_STORAGE_BLOCK
     *                         SHADER_STORAGE_BLOCK}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE
     *                         VERTEX_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE
     *                         TESS_CONTROL_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE
     *                         TESS_EVALUATION_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE
     *                         GEOMETRY_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE
     *                         FRAGMENT_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE
     *                         COMPUTE_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE_UNIFORM
     *                         VERTEX_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE_UNIFORM
     *                         TESS_CONTROL_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE_UNIFORM
     *                         TESS_EVALUATION_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE_UNIFORM
     *                         GEOMETRY_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE_UNIFORM
     *                         FRAGMENT_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE_UNIFORM
     *                         COMPUTE_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_TRANSFORM_FEEDBACK_VARYING
     *                         TRANSFORM_FEEDBACK_VARYING}</td>
     *                         <td>{@link GL42#GL_ATOMIC_COUNTER_BUFFER
     *                         ATOMIC_COUNTER_BUFFER}</td>
     *                         </tr>
     *                         </table>
     * @param pname            the name of the parameter within
     *                         {@code programInterface} to query. One of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_ACTIVE_RESOURCES
     *                         ACTIVE_RESOURCES}</td>
     *                         <td>{@link GL43C#GL_MAX_NAME_LENGTH
     *                         MAX_NAME_LENGTH}</td>
     *                         <td>{@link GL43C#GL_MAX_NUM_ACTIVE_VARIABLES
     *                         MAX_NUM_ACTIVE_VARIABLES}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_MAX_NUM_COMPATIBLE_SUBROUTINES
     *                         MAX_NUM_COMPATIBLE_SUBROUTINES}</td>
     *                         </tr>
     *                         </table>
     */
    int glGetProgramInterfacei(int program, int programInterface, int pname);

    /**
     * Queries the index of a named resource within a program.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the resource named {Wcode
     *                         name}. One of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_UNIFORM UNIFORM}</td>
     *                         <td>{@link GL43C#GL_UNIFORM_BLOCK UNIFORM_BLOCK}</td>
     *                         <td>{@link GL43C#GL_PROGRAM_INPUT PROGRAM_INPUT}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         <td>{@link GL43C#GL_BUFFER_VARIABLE
     *                         BUFFER_VARIABLE}</td>
     *                         <td>{@link GL43C#GL_SHADER_STORAGE_BLOCK
     *                         SHADER_STORAGE_BLOCK}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE
     *                         VERTEX_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE
     *                         TESS_CONTROL_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE
     *                         TESS_EVALUATION_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE
     *                         GEOMETRY_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE
     *                         FRAGMENT_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE
     *                         COMPUTE_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE_UNIFORM
     *                         VERTEX_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE_UNIFORM
     *                         TESS_CONTROL_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE_UNIFORM
     *                         TESS_EVALUATION_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE_UNIFORM
     *                         GEOMETRY_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE_UNIFORM
     *                         FRAGMENT_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE_UNIFORM
     *                         COMPUTE_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_TRANSFORM_FEEDBACK_VARYING
     *                         TRANSFORM_FEEDBACK_VARYING}</td>
     *                         <td>{@link GL42#GL_ATOMIC_COUNTER_BUFFER
     *                         ATOMIC_COUNTER_BUFFER}</td>
     *                         </tr>
     *                         </table>
     * @param name             the name of the resource to query the index of
     */
    int glGetProgramResourceIndex(int program, int programInterface, ByteBuffer name);

    /**
     * Queries the index of a named resource within a program.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the resource named {Wcode
     *                         name}. One of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_UNIFORM UNIFORM}</td>
     *                         <td>{@link GL43C#GL_UNIFORM_BLOCK UNIFORM_BLOCK}</td>
     *                         <td>{@link GL43C#GL_PROGRAM_INPUT PROGRAM_INPUT}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         <td>{@link GL43C#GL_BUFFER_VARIABLE
     *                         BUFFER_VARIABLE}</td>
     *                         <td>{@link GL43C#GL_SHADER_STORAGE_BLOCK
     *                         SHADER_STORAGE_BLOCK}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE
     *                         VERTEX_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE
     *                         TESS_CONTROL_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE
     *                         TESS_EVALUATION_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE
     *                         GEOMETRY_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE
     *                         FRAGMENT_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE
     *                         COMPUTE_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE_UNIFORM
     *                         VERTEX_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE_UNIFORM
     *                         TESS_CONTROL_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE_UNIFORM
     *                         TESS_EVALUATION_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE_UNIFORM
     *                         GEOMETRY_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE_UNIFORM
     *                         FRAGMENT_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE_UNIFORM
     *                         COMPUTE_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_TRANSFORM_FEEDBACK_VARYING
     *                         TRANSFORM_FEEDBACK_VARYING}</td>
     *                         <td>{@link GL42#GL_ATOMIC_COUNTER_BUFFER
     *                         ATOMIC_COUNTER_BUFFER}</td>
     *                         </tr>
     *                         </table>
     * @param name             the name of the resource to query the index of
     */
    int glGetProgramResourceIndex(int program, int programInterface, CharSequence name);

    /**
     * Queries the name of an indexed resource within a program.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the indexed resource. One
     *                         of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_UNIFORM UNIFORM}</td>
     *                         <td>{@link GL43C#GL_UNIFORM_BLOCK UNIFORM_BLOCK}</td>
     *                         <td>{@link GL43C#GL_PROGRAM_INPUT PROGRAM_INPUT}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         <td>{@link GL43C#GL_BUFFER_VARIABLE
     *                         BUFFER_VARIABLE}</td>
     *                         <td>{@link GL43C#GL_SHADER_STORAGE_BLOCK
     *                         SHADER_STORAGE_BLOCK}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE
     *                         VERTEX_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE
     *                         TESS_CONTROL_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE
     *                         TESS_EVALUATION_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE
     *                         GEOMETRY_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE
     *                         FRAGMENT_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE
     *                         COMPUTE_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE_UNIFORM
     *                         VERTEX_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE_UNIFORM
     *                         TESS_CONTROL_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE_UNIFORM
     *                         TESS_EVALUATION_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE_UNIFORM
     *                         GEOMETRY_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE_UNIFORM
     *                         FRAGMENT_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE_UNIFORM
     *                         COMPUTE_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_TRANSFORM_FEEDBACK_VARYING
     *                         TRANSFORM_FEEDBACK_VARYING}</td>
     *                         <td>{@link GL42#GL_ATOMIC_COUNTER_BUFFER
     *                         ATOMIC_COUNTER_BUFFER}</td>
     *                         </tr>
     *                         </table>
     * @param index            the index of the resource within
     *                         {@code programInterface} of {@code program}
     * @param length           a variable which will receive the length of the
     *                         resource name
     * @param name             a character array into which will be written the name
     *                         of the resource
     */
    void glGetProgramResourceName(int program, int programInterface, int index, IntBuffer length, ByteBuffer name);

    /**
     * Queries the name of an indexed resource within a program.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the indexed resource. One
     *                         of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_UNIFORM UNIFORM}</td>
     *                         <td>{@link GL43C#GL_UNIFORM_BLOCK UNIFORM_BLOCK}</td>
     *                         <td>{@link GL43C#GL_PROGRAM_INPUT PROGRAM_INPUT}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         <td>{@link GL43C#GL_BUFFER_VARIABLE
     *                         BUFFER_VARIABLE}</td>
     *                         <td>{@link GL43C#GL_SHADER_STORAGE_BLOCK
     *                         SHADER_STORAGE_BLOCK}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE
     *                         VERTEX_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE
     *                         TESS_CONTROL_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE
     *                         TESS_EVALUATION_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE
     *                         GEOMETRY_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE
     *                         FRAGMENT_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE
     *                         COMPUTE_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE_UNIFORM
     *                         VERTEX_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE_UNIFORM
     *                         TESS_CONTROL_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE_UNIFORM
     *                         TESS_EVALUATION_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE_UNIFORM
     *                         GEOMETRY_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE_UNIFORM
     *                         FRAGMENT_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE_UNIFORM
     *                         COMPUTE_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_TRANSFORM_FEEDBACK_VARYING
     *                         TRANSFORM_FEEDBACK_VARYING}</td>
     *                         <td>{@link GL42#GL_ATOMIC_COUNTER_BUFFER
     *                         ATOMIC_COUNTER_BUFFER}</td>
     *                         </tr>
     *                         </table>
     * @param index            the index of the resource within
     *                         {@code programInterface} of {@code program}
     * @param bufSize          the size of the character array whose address is
     *                         given by {@code name}
     */
    String glGetProgramResourceName(int program, int programInterface, int index, int bufSize);

    /**
     * Queries the name of an indexed resource within a program.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the indexed resource. One
     *                         of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_UNIFORM UNIFORM}</td>
     *                         <td>{@link GL43C#GL_UNIFORM_BLOCK UNIFORM_BLOCK}</td>
     *                         <td>{@link GL43C#GL_PROGRAM_INPUT PROGRAM_INPUT}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         <td>{@link GL43C#GL_BUFFER_VARIABLE
     *                         BUFFER_VARIABLE}</td>
     *                         <td>{@link GL43C#GL_SHADER_STORAGE_BLOCK
     *                         SHADER_STORAGE_BLOCK}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE
     *                         VERTEX_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE
     *                         TESS_CONTROL_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE
     *                         TESS_EVALUATION_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE
     *                         GEOMETRY_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE
     *                         FRAGMENT_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE
     *                         COMPUTE_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE_UNIFORM
     *                         VERTEX_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE_UNIFORM
     *                         TESS_CONTROL_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE_UNIFORM
     *                         TESS_EVALUATION_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE_UNIFORM
     *                         GEOMETRY_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE_UNIFORM
     *                         FRAGMENT_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE_UNIFORM
     *                         COMPUTE_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_TRANSFORM_FEEDBACK_VARYING
     *                         TRANSFORM_FEEDBACK_VARYING}</td>
     *                         <td>{@link GL42#GL_ATOMIC_COUNTER_BUFFER
     *                         ATOMIC_COUNTER_BUFFER}</td>
     *                         </tr>
     *                         </table>
     * @param index            the index of the resource within
     *                         {@code programInterface} of {@code program}
     */
    String glGetProgramResourceName(int program, int programInterface, int index);

    /**
     * Retrieves values for multiple properties of a single active resource within a
     * program object.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the resource named
     *                         {@code name}. One of:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_UNIFORM UNIFORM}</td>
     *                         <td>{@link GL43C#GL_UNIFORM_BLOCK UNIFORM_BLOCK}</td>
     *                         <td>{@link GL43C#GL_PROGRAM_INPUT PROGRAM_INPUT}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         <td>{@link GL43C#GL_BUFFER_VARIABLE
     *                         BUFFER_VARIABLE}</td>
     *                         <td>{@link GL43C#GL_SHADER_STORAGE_BLOCK
     *                         SHADER_STORAGE_BLOCK}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE
     *                         VERTEX_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE
     *                         TESS_CONTROL_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE
     *                         TESS_EVALUATION_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE
     *                         GEOMETRY_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE
     *                         FRAGMENT_SUBROUTINE}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE
     *                         COMPUTE_SUBROUTINE}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_VERTEX_SUBROUTINE_UNIFORM
     *                         VERTEX_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_CONTROL_SUBROUTINE_UNIFORM
     *                         TESS_CONTROL_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_TESS_EVALUATION_SUBROUTINE_UNIFORM
     *                         TESS_EVALUATION_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_GEOMETRY_SUBROUTINE_UNIFORM
     *                         GEOMETRY_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_FRAGMENT_SUBROUTINE_UNIFORM
     *                         FRAGMENT_SUBROUTINE_UNIFORM}</td>
     *                         <td>{@link GL43C#GL_COMPUTE_SUBROUTINE_UNIFORM
     *                         COMPUTE_SUBROUTINE_UNIFORM}</td>
     *                         </tr>
     *                         <tr>
     *                         <td>{@link GL43C#GL_TRANSFORM_FEEDBACK_VARYING
     *                         TRANSFORM_FEEDBACK_VARYING}</td>
     *                         <td>{@link GL42#GL_ATOMIC_COUNTER_BUFFER
     *                         ATOMIC_COUNTER_BUFFER}</td>
     *                         </tr>
     *                         </table>
     * @param index            the active resource index
     * @param props            an array that will receive the active resource
     *                         properties
     * @param length           a variable which will receive the number of values
     *                         returned
     * @param params           an array that will receive the property values
     */
    void glGetProgramResourceiv(int program, int programInterface, int index, IntBuffer props, IntBuffer length,
            IntBuffer params);

    /**
     * Queries the location of a named resource within a program.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the resource named
     *                         {@code name}
     * @param name             the name of the resource to query the location of
     */
    int glGetProgramResourceLocation(int program, int programInterface, ByteBuffer name);

    /**
     * Queries the location of a named resource within a program.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the resource named
     *                         {@code name}
     * @param name             the name of the resource to query the location of
     */
    int glGetProgramResourceLocation(int program, int programInterface, CharSequence name);

    /**
     * Queries the fragment color index of a named variable within a program.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the resource named
     *                         {@code name}. Must be:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         </tr>
     *                         </table>
     * @param name             the name of the resource to query the location of
     */
    // Not available in ES
    // int glGetProgramResourceLocationIndex(int program, int programInterface,
    // ByteBuffer name);

    /**
     * Queries the fragment color index of a named variable within a program.
     *
     * @param program          the name of a program object whose resources to query
     * @param programInterface a token identifying the interface within
     *                         {@code program} containing the resource named
     *                         {@code name}. Must be:<br>
     *                         <table>
     *                         <tr>
     *                         <td>{@link GL43C#GL_PROGRAM_OUTPUT
     *                         PROGRAM_OUTPUT}</td>
     *                         </tr>
     *                         </table>
     * @param name             the name of the resource to query the location of
     */
    // Not available in ES
     //   int glGetProgramResourceLocationIndex(int program, int programInterface, CharSequence name);

    /** Array version of: {@link #glGetProgramInterfaceiv GetProgramInterfaceiv} */
    void glGetProgramInterfaceiv(int program, int programInterface, int pname, int[] params);

    /**
     * Array version of: {@link #glGetProgramResourceName GetProgramResourceName}
     */
    void glGetProgramResourceName(int program, int programInterface, int index, int[] length, ByteBuffer name);

    /** Array version of: {@link #glGetProgramResourceiv GetProgramResourceiv} */
    void glGetProgramResourceiv(int program, int programInterface, int index, int[] props, int[] length, int[] params);

    default String name() {
        return "ProgramInterfaceQuery";
    }
}